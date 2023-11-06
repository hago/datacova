/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.verify

import com.google.gson.Gson
import com.hagoapp.datacova.utility.CoVaException
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.f2t.util.DateTimeTypeUtils
import com.hagoapp.datacova.surveyor.rule.EmbedJsRuleConfig
import com.hagoapp.datacova.surveyor.EmbedJsFunctionHelper
import com.hagoapp.datacova.surveyor.EmbedPythonHelper
import com.hagoapp.datacova.utility.StackTraceWriter
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

class ScriptOps {

    private val logger = LoggerFactory.getLogger(ScriptOps::class.java)

    enum class EvalDataType {
        Number,
        Boolean,
        DateTime,
        Text
    }

    data class EvalFieldData(
        val value: String,
        val type: EvalDataType
    ) {
        fun getTypedValue(): Any {
            return when (type) {
                EvalDataType.Number -> if (value.indexOf('.') > 0) value.toDouble() else value.toLong()
                EvalDataType.Boolean -> value.toBoolean()
                EvalDataType.DateTime -> DateTimeTypeUtils.stringToDateTimeOrNull(value)!!
                EvalDataType.Text -> value
            }
        }
    }

    data class EvaluateData4Python(
        val code: String,
        val fieldValues: Map<String, EvalFieldData>
    )

    @WebEndPoint(
        methods = [MethodName.POST],
        path = "/api/python/evaluate",
        authTypes = [AuthType.USER_TOKEN]
    )
    fun evaluatePython(context: RoutingContext) {
        val data = Gson().fromJson(context.body().asString(), EvaluateData4Python::class.java)
        if (data.code.isBlank()) {
            ResponseHelper.sendResponse(
                context, BAD_REQUEST, mapOf(
                    "code" to BAD_REQUEST.code(),
                    "error" to mapOf(
                        "message" to "Empty code snippet"
                    )
                )
            )
            return
        }
        try {
            val ret = EmbedPythonHelper.execCodeBlockOnce(
                data.code, mapOf(
                    "row" to data.fieldValues.map {
                        Pair(it.key, it.value.getTypedValue())
                    }.toMap()
                ), setOf("result")
            )
            println(ret)
            val x = ret["result"] ?: throw CoVaException("no variable \"ret\" defined")
            if (x !is Boolean) {
                throw CoVaException("the variable \"ret\" should be boolean")
            }
            ResponseHelper.sendResponse(
                context, OK, mapOf(
                    "code" to 0,
                    "data" to ret
                )
            )
        } catch (ex: Exception) {
            StackTraceWriter.write(ex, logger)
            ResponseHelper.respondError(context, BAD_REQUEST, ex.message)
        }
    }

    data class EvaluateData4Js (
        val config: EmbedJsRuleConfig,
        val params: List<Any?>
    )

    @WebEndPoint(
        methods = [MethodName.POST],
        path = "/api/js/evaluate",
        authTypes = [AuthType.USER_TOKEN]
    )
    fun evaluateJs(context: RoutingContext) {
        val data = Gson().fromJson(context.body().asString(), EvaluateData4Js::class.java)
        if (data.config.snippet.isBlank()) {
            ResponseHelper.sendResponse(
                context, BAD_REQUEST, mapOf(
                    "code" to BAD_REQUEST.code(),
                    "error" to mapOf(
                        "message" to "Empty code snippet"
                    )
                )
            )
            return
        }
        EmbedJsFunctionHelper(data.config.snippet).use { js ->
            val r = js.execute(*data.params.toTypedArray())
            ResponseHelper.sendResponse(context, OK, mapOf(
                "code" to 0,
                "data" to r
            ))
        }
    }
}
