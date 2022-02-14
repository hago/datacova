/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.web

import com.google.gson.GsonBuilder
import com.hagoapp.datacova.config.init.CoVaConfig
import com.hagoapp.datacova.data.redis.JedisManager
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.util.Utils
import com.hagoapp.datacova.util.http.ResponseHelper
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.Cookie
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import java.lang.Exception

class AuthUtils {
    companion object {
        const val LOGIN_COOKIE = "MfIry5adA8UYJEYb"
        private const val IMPERSONATOR_COOKIE = "5fVDH0qFaHbrcJv5"
        private const val TOKEN_AUTH_NAME = "name"
        private const val IMPERSONATE_TOKEN_HEADER_NAME = "TmzSOsex"
        private val gson = GsonBuilder().serializeNulls().create()

        private fun fromJson(json: String?): UserInfo? {
            return try {
                gson.fromJson(json, UserInfo::class.java)
            } catch (ex: Exception) {
                null
            }
        }

        private fun toJson(userInfo: UserInfo): String {
            return gson.toJson(userInfo)
        }

        fun loginUser(context: RoutingContext, user: UserInfo): String {
            val identity = Utils.genRandomString(12, null)
            JedisManager.getJedis(CoVaConfig.getConfig().redis).use { jedis ->
                jedis.setex(identity, 86400L, toJson(user))
            }
            val cookie = Cookie.cookie(LOGIN_COOKIE, identity).setPath("/")
            context.addCookie(cookie)
            return identity
        }

        fun logoutCurrentUser(context: RoutingContext) {
            val token = getCurrentToken(context)
            JedisManager.getJedis(CoVaConfig.getConfig().redis).use { it.del(token) }
            context.removeCookie(LOGIN_COOKIE)
        }

        fun setImpersonator(context: RoutingContext, impersonatedUser: UserInfo) {
            val identity = Utils.genRandomString(12, null)
            JedisManager.getJedis(CoVaConfig.getConfig().redis).use {
                it.setex(identity, 86400L, toJson(impersonatedUser))
            }
            val cookie = Cookie.cookie(IMPERSONATOR_COOKIE, identity).setPath("/")
            context.addCookie(cookie)
        }

        fun clearImpersonator(context: RoutingContext) {
            val token = getCurrentToken(context)
            JedisManager.getJedis(CoVaConfig.getConfig().redis).use { it.del(token) }
            context.removeCookie(IMPERSONATOR_COOKIE)
        }

        fun getCurrentUser(context: RoutingContext, jumpPath: String? = null): UserInfo? {
            val userJson = when (val token = getCurrentToken(context)) {
                null -> null
                else -> JedisManager.getJedis(CoVaConfig.getConfig().redis).use {
                    it.get(token)
                }
            }
            val user = fromJson(userJson)
            return when {
                user != null -> user
                jumpPath == null -> null
                else -> {
                    ResponseHelper.sendResponse(
                        context,
                        HttpResponseStatus.FOUND,
                        mapOf("Location" to jumpPath),
                        byteArrayOf()
                    )
                    null
                }
            }
        }

        fun getCurrentToken(context: RoutingContext): String? {
            val cookie = context.getCookie(LOGIN_COOKIE)
            if (cookie != null) {
                return cookie.value
            }
            val header = context.request().getHeader(HttpHeaders.AUTHORIZATION)
            return parseAuthTokenHeader(header)
        }

        fun parseAuthTokenHeader(header: String?): String? {
            return when (header) {
                null -> null
                else -> {
                    val parts = header.split(" ").map { it.trim() }
                    if ((parts.size == 2) && (parts[0].compareTo(TOKEN_AUTH_NAME) == 0)) parts[1]
                    else null
                }
            }
        }

        fun getImpersonateUser(context: RoutingContext): UserInfo? {
            val impJson = when (val identity = getImpersonateToken(context)) {
                null -> null
                else -> JedisManager.getJedis(CoVaConfig.getConfig().redis).use {
                    it.get(identity)
                }
            }
            return fromJson(impJson)
        }

        fun getImpersonateToken(context: RoutingContext): String? {
            val cookie = context.getCookie(IMPERSONATOR_COOKIE)
            if (cookie != null) {
                return cookie.value
            }
            val header = context.request().getHeader(IMPERSONATE_TOKEN_HEADER_NAME)
            if (header != null) {
                return header
            }
            return null
        }
    }
}