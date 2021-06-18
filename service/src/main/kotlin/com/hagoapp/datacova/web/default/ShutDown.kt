/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.default

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.executor.Executor
import com.hagoapp.datacova.util.http.RequestHelper
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.WebManager
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import edazdarevic.commons.net.CIDRUtils
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class ShutDown {

    companion object {
        private var isShuttingDown: AtomicBoolean = AtomicBoolean(false)
    }

    @WebEndPoint(
        methods = [HttpMethod.GET],
        authTypes = [AuthType.Anonymous],
        path = "/api/service/shutdown",
        isBlocking = false
    )
    fun shutDown(context: RoutingContext) {
        if (!canShutDown(context)) {
            ResponseHelper.respondError(context, HttpResponseStatus.UNAUTHORIZED, "NOT Allowed")
            return
        }
        if (isShuttingDown.acquire) {
            ResponseHelper.sendResponse(context, HttpResponseStatus.OK)
        }
        isShuttingDown.set(true)
        GlobalScope.launch {
            Executor.getExecutor().stop()
            WebManager.getManager(CoVaConfig.getConfig().web, listOf()).shutDownWebServer()
        }
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK)
    }

    @WebEndPoint(
        methods = [HttpMethod.GET],
        authTypes = [AuthType.Anonymous],
        path = "/api/service/shutdown/force",
        isBlocking = false
    )
    fun forceShutDown(context: RoutingContext) {
        if (!canShutDown(context)) {
            ResponseHelper.respondError(context, HttpResponseStatus.UNAUTHORIZED, "NOT Allowed")
            return
        }
        if (isShuttingDown.acquire) {
            ResponseHelper.sendResponse(context, HttpResponseStatus.OK)
        }
        isShuttingDown.set(true)
        GlobalScope.launch {
            Executor.getExecutor().stop(true)
            WebManager.getManager(CoVaConfig.getConfig().web, listOf()).shutDownWebServer()
        }
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK)
    }

    private fun canShutDown(context: RoutingContext): Boolean {
        val ip = RequestHelper.getRemoteIp(context)
        return ip.equals(context.request().localAddress().host()) ||
                CoVaConfig.getConfig().web.privilegedIpAddresses.any {
                    if (it.contains('/')) CIDRUtils(it).isInRange(ip) else it.equals(ip)
                }
    }
}
