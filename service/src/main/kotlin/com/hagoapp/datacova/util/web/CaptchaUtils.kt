/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.web

import com.hagoapp.datacova.util.Utils
import com.wf.captcha.base.Captcha
import com.wf.captcha.SpecCaptcha
import io.vertx.core.http.Cookie
import io.vertx.ext.web.RoutingContext
import java.io.ByteArrayOutputStream

class CaptchaUtils {

    interface CaptchaStorage {
        fun storeAssociation(identity: String, code: String, expiry: Int)
        fun findCode(identity: String): String?
    }

    data class CaptchaConfig(
        val width: Int,
        val height: Int,
        val length: Int
    )

    class CaptchaStorageMemory : CaptchaStorage {

        companion object {
            val cache = mutableMapOf<String, String>()
        }

        override fun storeAssociation(identity: String, code: String, expiry: Int) {
            cache[identity] = code
        }

        override fun findCode(identity: String): String? {
            return cache.remove(identity)
        }
    }

    companion object {
        private const val CAPTCHA_COOKIE = "7V8wXMZBKw9y"
        private const val CAPTCHA_EXPIRY = 300

        fun createCaptcha(
            routingContext: RoutingContext,
            config: CaptchaConfig,
            identity: String = Utils.genRandomString(12),
            storage: CaptchaStorage = CaptchaStorageMemory()
        ): Pair<String, ByteArray> {
            val captcha = SpecCaptcha(config.width, config.height, config.length)
            captcha.charType = Captcha.TYPE_NUM_AND_UPPER
            val code = captcha.text()
            val cookie = Cookie.cookie(CAPTCHA_COOKIE, identity)
            cookie.path = "/"
            routingContext.addCookie(cookie)
            storage.storeAssociation(identity, code, CAPTCHA_EXPIRY)
            ByteArrayOutputStream().use { baos ->
                captcha.out(baos)
                val img = baos.toByteArray()
                return Pair(code, img)
            }
        }

        fun verifyCaptcha(
            routingContext: RoutingContext,
            userInput: String,
            caseInSensitive: Boolean = false,
            storage: CaptchaStorage = CaptchaStorageMemory()
        ): Boolean {
            val cookie = routingContext.getCookie(CAPTCHA_COOKIE) ?: return false
            val identity = cookie.value ?: return false
            val code = storage.findCode(identity) ?: return false
            return userInput.compareTo(code, caseInSensitive) == 0
        }
    }
}