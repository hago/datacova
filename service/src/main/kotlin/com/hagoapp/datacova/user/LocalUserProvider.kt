/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.data.UserData
import com.hagoapp.datacova.util.web.CaptchaUtils
import io.vertx.ext.web.RoutingContext

class LocalUserProvider : UserAuthProvider {

    companion object {
        const val PROVIDER_NAME = "local_db"
        private const val USERNAME_FIELD = "userId"
        private const val PASSWORD_FIELD = "password"
        private const val CAPTCHA_FIELD = "captcha"
    }

    private val dbConfig = CoVaConfig.getConfig().database
    private val logger = CoVaLogger.getLogger()

    override fun authenticate(context: RoutingContext): UserInfo? {
        val req = context.request()
        val password = req.getParam(PASSWORD_FIELD)
        val userId = req.getParam(USERNAME_FIELD)
        val captcha = req.getParam(CAPTCHA_FIELD)
        if (!CaptchaUtils.verifyCaptcha(context, captcha)) {
            logger.error("Captcha error when user {} try to login", userId)
            return null
        }
        logger.debug("12345")
        val userInfo = getUserInfo(userId)
        return when {
            userInfo == null -> {
                logger.error("user not found for {} when try to login", userId)
                null
            }
            userInfo.pwdHash != UserData.computePwdHash(password) -> {
                logger.error("password mismatch when user {} try to login", userId)
                null
            }
            else -> {
                logger.info("user {} log in", userId)
                userInfo
            }
        }
    }

    override fun getProviderName(): String {
        return PROVIDER_NAME
    }

    override fun getUserInfo(userId: String): UserInfo? {
        return RedisCacheReader.readCachedData(
            "UserInfo", 60 * 30,
            userInfoLoader, UserInfo::class.java, userId
        )
    }

    private val userInfoLoader = object : RedisCacheReader.GenericLoader<UserInfo> {
        override fun perform(vararg params: Any?): UserInfo? {
            UserData(dbConfig).use { return it.findUser(params[0] as String) }
        }

    }
}