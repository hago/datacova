/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.executor.web

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.execution.TaskExecutionData
import com.hagoapp.datacova.entity.execution.ExecutionStatus
import com.hagoapp.datacova.execution.Worker
import com.hagoapp.datacova.executor.Executor
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class ExecutorApi {
    @WebEndPoint(
        path = "/api/executor/execute/:id",
        methods = [HttpMethod.POST]
    )
    fun execute(context: RoutingContext) {
        val id = context.pathParam("id").toInt()
        val te = TaskExecutionData().getTaskExecution(id)
        if ((te == null) || (te.status != ExecutionStatus.added)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "execution $id is not for running")
            return
        }
        val workerThread = Thread(Runnable {
            Worker(te).execute()
        })
        workerThread.isDaemon = true
        workerThread.start()
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0))
    }

    @WebEndPoint(
        path = "/api/executor/execute/status",
        methods = [HttpMethod.GET]
    )
    fun status(context: RoutingContext) {
        val statuses = Executor.getExecutor()!!.getExecutionStatuses()
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to mapOf(
                    "executor" to CoVaConfig.getConfig().executor,
                    "executions" to statuses
                )
            )
        )
    }
}
