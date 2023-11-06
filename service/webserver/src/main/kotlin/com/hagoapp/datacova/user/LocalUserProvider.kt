/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user

import com.hagoapp.datacova.utility.Utils
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.user.UserData
import com.hagoapp.datacova.util.FileStoreUtils
import com.hagoapp.datacova.util.web.CaptchaUtils
import com.hagoapp.datacova.utility.redis.RedisCacheReader
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.util.*

class LocalUserProvider : UserAuthProvider {

    companion object {
        const val PROVIDER_NAME = "local_db"
        private const val USERNAME_FIELD = "userId"
        private const val PASSWORD_FIELD = "password"
        private const val CAPTCHA_FIELD = "captcha"
        const val LOCAL_DATABASE_USER_TYPE = 0
    }

    private val dbConfig = CoVaConfig.getConfig().database
    private val logger = LoggerFactory.getLogger(LocalUserProvider::class.java)

    override fun authenticate(context: RoutingContext): UserInfo? {
        val req = context.request()
        val password = req.getParam(PASSWORD_FIELD)
        val userId = req.getParam(USERNAME_FIELD)
        val captcha = req.getParam(CAPTCHA_FIELD)
        if (!CaptchaUtils.verifyCaptcha(context, captcha, true)) {
            logger.error("Local Database user auth: Captcha error when user {} try to login", userId)
            return null
        }
        val userInfo = getUserInfo(userId)
        return when {
            userInfo == null -> {
                logger.error("Local Database user auth: user not found for {} when try to login", userId)
                null
            }

            userInfo.status == UserStatus.Registered -> {
                logger.error("Local Database user auth: user not activated for {} when try to login", userId)
                null
            }

            userInfo.status == UserStatus.Deleted -> {
                logger.error("Local Database user auth: user has been deleted for {} when try to login", userId)
                null
            }

            userInfo.pwdHash != UserData.computePwdHash(password) -> {
                logger.error("Local Database user auth: password mismatch when user '{} try to login", userId)
                null
            }

            else -> {
                logger.info("Local Database user auth: user '{}' log in", userId)
                userInfo
            }
        }
    }

    override fun getProviderName(): String {
        return PROVIDER_NAME
    }

    override fun getProviderType(): Int {
        return LOCAL_DATABASE_USER_TYPE
    }

    override fun getUserInfo(userId: String): UserInfo? {
        return RedisCacheReader.readCachedData(
            "UserInfo", 60 * 30,
            userInfoLoader, UserInfo::class.java, userId
        )
    }

    override fun loadThumbnail(userInfo: UserInfo) {
        val path = calcThumbPath(userInfo.userId)
        val fs = FileStoreUtils.getThumbnailFileStore()
        val buffer = fs.readFileInStore(path)
        if (buffer != null) {
            userInfo.thumbnail = Base64.getEncoder().encodeToString(buffer)
        }
    }

    override fun saveThumbnail(userId: String, thumbnail: ByteArray): String {
        val fs = FileStoreUtils.getThumbnailFileStore()
        val targetName = calcThumbPath(userId)
        ByteArrayInputStream(thumbnail).use { fs.saveFileToStore(targetName, it) }
        return targetName
    }

    override fun searchUser(search: String, count: Int): List<UserSearchResultItem> {
        throw UnsupportedOperationException("Local user provider contains users from others, don't call it here!")
    }

    private fun calcThumbPath(userId: String): String {
        val hash = Utils.md5Digest(userId)
        return "${hash.substring(0, 3)}/${hash.substring(3, 6)}/$hash"
    }

    private val userInfoLoader = { params: Array<Any?> ->
        UserData(dbConfig).use { it.findUser(params[0] as String) }
    }
}
