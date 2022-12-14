/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.verify

import com.google.gson.Gson
import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.util.EmbedPythonHelper
import com.hagoapp.datacova.util.LuaHelper
import com.hagoapp.datacova.util.StackTraceWriter
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.f2t.util.DateTimeTypeUtils
import io.netty.handler.codec.http.HttpResponseStatus.*
import io.vertx.ext.web.RoutingContext
import org.luaj.vm2.LuaError
import org.luaj.vm2.lib.jse.JsePlatform
import org.slf4j.LoggerFactory

class ScriptOps {

    private val logger = LoggerFactory.getLogger(ScriptOps::class.java)

    @WebEndPoint(
        methods = [MethodName.POST],
        path = "/api/lua/evaluate",
        authTypes = [AuthType.UserToken]
    )
    fun evaluateLua(context: RoutingContext) {
        val data = Gson().fromJson(context.body().asString(), EvaluateData::class.java)
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
        val code = data.code.replace("\\r", "\r").replace("\\n", "\n")
        try {
            val chunk = JsePlatform.debugGlobals().load(code)
            val arg = LuaHelper.mapToLuaTable(data.fieldValues)
            val ret = chunk.call(arg)
            ResponseHelper.sendResponse(
                context, OK, mapOf(
                    "code" to 0,
                    "data" to ret.toString()
                )
            )
        } catch (ex: LuaError) {
            StackTraceWriter.write(ex, logger)
            ResponseHelper.respondError(context, BAD_REQUEST, ex.message)
        } catch (ex: Exception) {
            ResponseHelper.respondError(context, INTERNAL_SERVER_ERROR, ex.message)
        }
    }

    data class EvaluateData(
        val code: String,
        val fieldValues: Map<String, String>
    )

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
        authTypes = [AuthType.UserToken]
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
}
