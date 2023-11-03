/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.user

import com.google.gson.Gson
import com.hagoapp.datacova.Utils
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.data.user.UserData
import com.hagoapp.datacova.user.LocalUserProvider
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.util.http.RequestHelper
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.util.mail.MailHelper
import com.hagoapp.datacova.util.text.TemplateManager
import com.hagoapp.datacova.util.text.TextResourceManager
import com.hagoapp.datacova.util.web.CaptchaUtils
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import java.io.StringWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.mail.internet.InternetAddress

class Register {

    companion object {
        private const val REGISTRATION_CODE_EXPIRE_SECONDS = 86400L
    }

    @WebEndPoint(
        methods = [MethodName.PUT],
        path = "/api/register/:captcha",
        authTypes = [AuthType.ANONYMOUS]
    )
    fun register(context: RoutingContext) {
        val captcha = context.pathParam("captcha")
        if (!CaptchaUtils.verifyCaptcha(context, captcha, false)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "captcha error")
            return
        }
        val json = context.body().asString()
        val user = Gson().fromJson(json, UserInfo::class.java)
        val dal = UserData()
        if (dal.isUserIdExisted(user.userId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.CONFLICT, "duplicate user id ${user.userId}")
            return
        }
        if (dal.isEmailExisted(user.email)) {
            ResponseHelper.respondError(context, HttpResponseStatus.CONFLICT, "duplicate email ${user.email}")
            return
        }
        if (dal.isMobileExisted(user.mobile)) {
            ResponseHelper.respondError(context, HttpResponseStatus.CONFLICT, "duplicate mobile ${user.mobile}")
            return
        }
        user.pwdHash = UserData.computePwdHash(user.pwdHash)
        user.thumbnail = saveThumbnail(user.userId, user.thumbnail)
        val u = dal.registerUser(user)
        sendActivation(context, u, Locale.getDefault())
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to u
            )
        )
    }

    private fun saveThumbnail(userId: String, b64stringThumb: String): String {
        val buffer = Base64.getDecoder().decode(b64stringThumb)
        return LocalUserProvider().saveThumbnail(userId, buffer)
    }

    private fun sendActivation(context: RoutingContext, user: UserInfo, locale: Locale) {
        val resourceName = "user/register/activation"
        val tm = TextResourceManager.getManager()
        val manager = TemplateManager.getResourcedTemplateManager()
        val template = manager.getTemplate(resourceName, locale)
        val sw = StringWriter()
        template!!.process(
            mapOf(
                "user" to user,
                "homepage" to getBaseUrl(context),
                "activateurl" to String.format("%s/user/activate/%s", getBaseUrl(context), createActivationCode(user)),
                "expire" to calcExpireDate()
            ), sw
        )
        val m = MailHelper(CoVaConfig.getConfig().mail)
        m.addRecipient(InternetAddress(user.email))
        m.setTitle(tm.getString(locale, resourceName)!!)
        m.setHtmlContent(sw.toString())
        m.sendMail()
    }

    private fun getBaseUrl(context: RoutingContext): String {
        return CoVaConfig.getConfig().web.baseUrl ?: RequestHelper.getBaseUrl(context)
    }

    private fun createActivationCode(user: UserInfo): String {
        val code = Utils.genRandomString(16, null)
        UserCache.saveUserRegistrationCode(user, code, REGISTRATION_CODE_EXPIRE_SECONDS)
        return code
    }

    private fun calcExpireDate(): String {
        val fmt = DateTimeFormatter.ISO_DATE_TIME
        return fmt.format(ZonedDateTime.now().plusSeconds(REGISTRATION_CODE_EXPIRE_SECONDS))
    }

    @WebEndPoint(
        path = "/api/user/activate/:code",
        methods = [MethodName.GET],
        authTypes = [AuthType.ANONYMOUS]
    )
    fun activate(context: RoutingContext) {
        val code = context.pathParam("code")
        val id = UserCache.getUserIdByRegistrationCode(code)
        if (id == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.NOT_FOUND, "invalid code")
            return
        }
        if (UserData().activateUser(id) > 0) {
            ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0))
        } else {
            ResponseHelper.respondError(context, HttpResponseStatus.NOT_FOUND, "invalid code")
        }
    }
}
