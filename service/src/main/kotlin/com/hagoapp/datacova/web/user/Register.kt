/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.user

import com.google.gson.Gson
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.user.UserData
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.util.FileStoreUtils
import com.hagoapp.datacova.util.Utils
import com.hagoapp.datacova.util.http.RequestHelper
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.util.mail.MailHelper
import com.hagoapp.datacova.util.text.TemplateManager
import com.hagoapp.datacova.util.text.TextResourceManager
import com.hagoapp.datacova.util.web.CaptchaUtils
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import java.io.ByteArrayInputStream
import java.io.StringWriter
import java.util.*
import javax.mail.internet.InternetAddress

class Register {

    @WebEndPoint(
        methods = [HttpMethod.PUT],
        path = "/api/register/:captcha",
        authTypes = [AuthType.Anonymous]
    )
    fun register(context: RoutingContext) {
        val captcha = context.pathParam("captcha")
        if (!CaptchaUtils.verifyCaptcha(context, captcha, false)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "captcha error")
            return
        }
        val json = context.bodyAsString
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
        user.thumbnail = saveThumbnail(user.id.toString(), user.thumbnail)
        val u = dal.registerUser(user)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to u
            )
        )
    }

    private fun saveThumbnail(thumbName: String, b64stringThumb: String): String {
        val buffer = Base64.getDecoder().decode(b64stringThumb)
        val fs = FileStoreUtils.getThumbnailFileStore()
        val hash = Utils.md5Digest(thumbName)
        val targetName = "${hash.substring(0, 3)}/${hash.substring(3, 6)}/$hash"
        ByteArrayInputStream(buffer).use { fs.saveFileToStore(targetName, it) }
        return targetName
    }

    private fun sendActivation(context: RoutingContext, user: UserInfo, locale: Locale) {
        val resourceName = "user/register/activation"
        val tm = TextResourceManager.getManager()
        val manager = TemplateManager.getResourceTemplateManager()
        val template = manager.getTemplate(resourceName, locale)
        val sw = StringWriter()
        template!!.process(mapOf(
            "user" to user,
            "homepage" to RequestHelper.getBaseUrl(context),
            "activateurl" to String.format("%s/user/activate", RequestHelper.getBaseUrl(context))
        ), sw)
        val m = MailHelper(CoVaConfig.getConfig().mail)
        m.addRecipient(InternetAddress(user.email))
        m.setTitle(tm.getString(locale, resourceName)!!)
        m.setHtmlContent(sw.toString())
        //m.sendMail()
    }
}
