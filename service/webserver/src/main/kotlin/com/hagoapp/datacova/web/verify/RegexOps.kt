/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.verify

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.surveyor.rule.RegexRuleConfig
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

class RegexOps {

    @WebEndPoint(
        path = "/api/regex/evaluate",
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN]
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

    private data class EvalRequest(
        val regexConfig: RegexRuleConfig,
        val text: String
    )

    @WebEndPoint(
        path = "/api/rule/regex/evaluate",
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun verifyRegexRule(context: RoutingContext) {
        val s = context.body().asString()
        try {
            val req = Gson().fromJson(s, EvalRequest::class.java)
                ?: throw JsonSyntaxException("Not a valid regex eval data")
            val p: Pattern
            try {
                p = Pattern.compile(
                    req.regexConfig.pattern,
                    if (req.regexConfig.isCaseSensitive) 0 else Pattern.CASE_INSENSITIVE
                )
            } catch (e: PatternSyntaxException) {
                ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to -1))
                return
            }
            val m = p.matcher(req.text)
            ResponseHelper.sendResponse(
                context, HttpResponseStatus.OK, mapOf(
                    "code" to if (m.matches()) 0 else -2
                )
            )
        } catch (e: JsonSyntaxException) {
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
