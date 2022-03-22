/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.internal

import com.google.gson.Gson
import com.hagoapp.datacova.ExecutorManager
import com.hagoapp.datacova.config.ExecutorConfig
import com.hagoapp.datacova.entity.internal.ExecutorStatus
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class Dispatcher {
    @WebEndPoint(
        path = "/api/dispatcher/register",
        methods = [HttpMethod.POST]
    )
    fun register(context: RoutingContext) {
        val json = context.bodyAsString
        val config = Gson().fromJson(json, ExecutorConfig::class.java)
        if (config == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid body");
            return
        }
        val exe = ExecutorStatus()
        exe.executor = config
        exe.executions = listOf()
        ExecutorManager.getManager().registerExecutor(exe);
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0))
    }

    @WebEndPoint(
        path = "/api/dispatcher/heartbeat",
        methods = [HttpMethod.POST]
    )
    fun heartbeat(context: RoutingContext) {
        val json = context.bodyAsString
        val exe = ExecutorStatus.fromJson(json)
        if (exe == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid body");
            return
        }
        ExecutorManager.getManager().keepAlive(exe);
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0))
    }

    @WebEndPoint(
        path = "/api/execute/status",
        methods = [HttpMethod.GET]
    )
    fun status(context: RoutingContext) {
        val executors = ExecutorManager.getManager().executors.values.sortedWith() { a, b ->
            when {
                a.lastActiveTime < b.lastActiveTime -> 1
                a.lastActiveTime > b.lastActiveTime -> -1
                a.status.executions.size < b.status.executions.size -> 1
                else -> -1
            }
        }
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to executors
            )
        )
    }
}
