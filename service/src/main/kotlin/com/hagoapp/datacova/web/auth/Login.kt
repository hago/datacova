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

class Login : WebInterface {
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
            CoVaLogger.getLogger().debug("/login")
            for (provider in UserAuthFactory.getFactory().availableAuthProviders()) {
                CoVaLogger.getLogger().debug("provider {}", provider.getProviderName())
                when (val userInfo = provider.authenticate(routeContext)) {
                    null -> continue
                    else -> {
                        loginSucceed(routeContext, userInfo)
                        return
                    }
                }
            }
            ResponseHelper.respondError(routeContext, HttpResponseStatus.FORBIDDEN, "authentication failed")
        }

        private fun loginSucceed(routeContext: RoutingContext, user: UserInfo) {
            val token = AuthUtils.loginUser(routeContext, user)
            ResponseHelper.sendResponse(
                routeContext, HttpResponseStatus.OK, mapOf(
                    "code" to 0,
                    "data" to mapOf("user" to user, "token" to token)
                )
            )
        }
    }


}