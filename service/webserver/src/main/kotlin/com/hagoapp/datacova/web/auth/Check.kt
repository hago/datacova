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
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.WebInterface
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

class Check : WebInterface {
    override fun getPath(): String {
        return "/api/auth/check"
    }

    private val logger = LoggerFactory.getLogger(Check::class.java)

    override fun requestHandlers(): MutableMap<String, WebInterface.Handler> {
        return mutableMapOf(
            MethodName.GET to WebInterface.Handler { context ->
                val user = AuthUtils.getCurrentUser(context)
                if (user == null) respondNotLogin(context) else respondLogin(context, user)
            }
        )
    }

    private fun respondNotLogin(context: RoutingContext) {
        ResponseHelper.respondError(context, HttpResponseStatus.UNAUTHORIZED, "Not logged")
    }

    private fun respondLogin(context: RoutingContext, user: UserInfo) {
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to mapOf(
                    "user" to user.maskUserInfo(),
                    "token" to AuthUtils.getCurrentToken(context)
                )
            )
        )
    }
}