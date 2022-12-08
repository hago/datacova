/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web.auth

import com.hagoapp.datacova.data.user.PermissionCache
import com.hagoapp.datacova.user.UserAuthFactory
import com.hagoapp.datacova.user.UserAuthProvider
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.permission.UserPermissions
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.util.web.AuthUtils
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.WebInterface
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

class Login : WebInterface {

    private val logger = LoggerFactory.getLogger(Login::class.java)

    override fun getPath(): String {
        return "/api/auth/login"
    }

    override fun requestHandlers(): MutableMap<String, WebInterface.Handler> {
        return mutableMapOf(
            MethodName.POST to respondFunc
        )
    }

    private val respondFunc = object : WebInterface.Handler {
        override fun handle(routeContext: RoutingContext) {
            val factory = UserAuthFactory.getFactory()
            if (routeContext.request().params().contains(LOGIN_PROVIDER)) {
                val providerType = routeContext.request().getParam(LOGIN_PROVIDER).toIntOrNull() ?: 0
                val provider = factory.getAuthProvider(providerType)
                val userInfo = provider.authenticate(routeContext)
                if (userInfo != null) {
                    loginSucceed(routeContext, userInfo, provider)
                    return
                }
            } else {
                for (providerCandidate in UserAuthFactory.getFactory().availableAuthProviders()) {
                    when (val userInfo = providerCandidate.authenticate(routeContext)) {
                        null -> continue
                        else -> {
                            loginSucceed(routeContext, userInfo, providerCandidate)
                            return
                        }
                    }
                }
            }
            ResponseHelper.respondError(routeContext, HttpResponseStatus.FORBIDDEN, "authentication failed")
        }

        private fun loginSucceed(routeContext: RoutingContext, user: UserInfo, provider: UserAuthProvider) {
            logger.debug("authenticate succeeded by provider {}", provider.getProviderName())
            val token = AuthUtils.loginUser(routeContext, user)
            val userPermission = PermissionCache.getUserPermissions(user)
            ResponseHelper.sendResponse(
                routeContext, HttpResponseStatus.OK, mapOf(
                    "code" to 0,
                    "data" to LoginUser(
                        user.maskUserInfo(),
                        token,
                        userPermission
                    )
                )
            )
        }
    }

    companion object {
        private const val LOGIN_PROVIDER = "provider"
    }

    data class LoginUser(
        val user: UserInfo,
        val token: String,
        val permission: UserPermissions
    )

}