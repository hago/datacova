/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web.auth

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.util.web.AuthUtils
import com.hagoapp.datacova.web.WebInterface
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import com.hagoapp.datacova.user.UserAuthFactory
import com.hagoapp.datacova.user.UserAuthProvider

class Login : WebInterface {

    private val logger = CoVaLogger.getLogger()

    override fun getPath(): String {
        return "/api/auth/login"
    }

    override fun requestHandlers(): MutableMap<HttpMethod, WebInterface.Handler> {
        return mutableMapOf(
            HttpMethod.POST to respondFunc
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
            ResponseHelper.sendResponse(
                routeContext, HttpResponseStatus.OK, mapOf(
                    "code" to 0,
                    "data" to mapOf("user" to user.maskUserInfo(), "token" to token)
                )
            )
        }
    }

    companion object {
        private const val LOGIN_PROVIDER = "provider"
    }


}