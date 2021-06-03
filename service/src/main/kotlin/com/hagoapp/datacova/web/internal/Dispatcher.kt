/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.internal

import com.hagoapp.datacova.ExecutorManager
import com.hagoapp.datacova.entity.Executor
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class Dispatcher {
    @WebEndPoint(
        path = "/executor/register",
        methods = [HttpMethod.POST]
    )
    fun register(context: RoutingContext) {
        val json = context.bodyAsString
        val exe = Executor.fromJson(json)
        if (exe == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid body");
            return
        }
        ExecutorManager.getManager().registerExecutor(exe);
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0))
    }
}
