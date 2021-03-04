/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web.auth

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
            val req = routeContext.request()
            val password = req.getParam("password")
            val userId = req.getParam("userId")
            val extra = req.params().getAll("extra")
            val providerName = req.getParam("provider")
            if ((userId == null) || (password == null)) {
                ResponseHelper.respondError(routeContext, HttpResponseStatus.BAD_REQUEST, "invalid arguments")
                return
            }
            if (providerName != null) {
                val provider = UserAuthFactory.getFactory().getAuthProvider(providerName)
                if (provider.authenticate(userId, password, *extra.toTypedArray())) {
                    val user = UserInfo(userId, provider.getProviderName())
                    loginSucceed(routeContext, user)
                    return
                }
            } else {
                for (provider in UserAuthFactory.getFactory().availableAuthProviders()) {
                    if (provider.isValidUserId(userId) &&
                        provider.authenticate(userId, password, *extra.toTypedArray())
                    ) {
                        val user = UserInfo(userId, provider.getProviderName())
                        loginSucceed(routeContext, user)
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
                    "data" to mapOf("com/hagoapp/datacova/user" to user, "token" to token)
                )
            )
        }
    }


}