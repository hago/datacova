/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.verify

import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class RegexOps {

    @WebEndPoint(
        path = "/api/regex/evaluate",
        methods = [MethodName.POST],
        authTypes = [AuthType.UserToken]
    )
    fun verifyRegex(context: RoutingContext) {
        val pattern = context.body().asString()
        try {
            Pattern.compile(pattern)
            ResponseHelper.sendResponse(context, HttpResponseStatus.OK)
        } catch (e: PatternSyntaxException) {
            ResponseHelper.sendResponse(
                context, HttpResponseStatus.BAD_REQUEST, mapOf(
                    "code" to HttpResponseStatus.BAD_REQUEST.code(),
                    "error" to mapOf(
                        "message" to e.message
                    )
                )
            )
        }
    }
}
