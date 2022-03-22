/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.executor.web

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.executor.Executor
import com.hagoapp.datacova.util.http.RequestHelper
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.WebManager
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import edazdarevic.commons.net.CIDRUtils
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean

class ShutDown {

    companion object {
        private var isShuttingDown: AtomicBoolean = AtomicBoolean(false)
    }

    @WebEndPoint(
        methods = [MethodName.GET],
        authTypes = [AuthType.Anonymous],
        path = "/api/service/executor/shutdown",
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
        context.addBodyEndHandler {
            Thread.sleep(1000)
            runBlocking {
                launch {
                    shutDown()
                }
            }
        }
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK)
    }

    @WebEndPoint(
        methods = [MethodName.GET],
        authTypes = [AuthType.Anonymous],
        path = "/api/service/executor/shutdown/force",
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
        context.addBodyEndHandler {
            Thread.sleep(1000)
            runBlocking {
                launch {
                    shutDown()
                }
            }
        }
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK)
    }

    private fun shutDown(force: Boolean = false) {
        val cfg = CoVaConfig.getConfig()
        if (cfg.executor != null) {
            Executor.getExecutor()?.stop(force)
        }
        if (cfg.web != null) {
            WebManager.getManager(cfg.web, listOf()).shutDownWebServer()
        }
    }

    private fun canShutDown(context: RoutingContext): Boolean {
        val ip = RequestHelper.getRemoteIp(context)
        return ip.equals(context.request().localAddress().host()) ||
                CoVaConfig.getConfig().web.privilegedIpAddresses.any {
                    if (it.contains('/')) CIDRUtils(it).isInRange(ip) else it.equals(ip)
                }
    }
}
