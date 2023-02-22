/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.JsonStringify
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.ExecutorConfig
import com.hagoapp.datacova.entity.internal.ExecutorStatus
import com.hagoapp.datacova.executor.Executor
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpHeaders
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

/**
 * This class is used by executor to invoke dispatcher api.
 */
class DispatcherInvoker(val config: ExecutorConfig) {

    constructor() : this(CoVaConfig.getConfig().executor)

    private val registerUrl = "${config.dispatcherUrl}/api/dispatcher/register"
    private val heartbeatUrl = "${config.dispatcherUrl}/api/dispatcher/heartbeat"
    private val logger = LoggerFactory.getLogger(DispatcherInvoker::class.java)

    fun register(): Boolean {
        return try {
            http(registerUrl, config)
            logger.info("executor registration succeeded")
            true
        } catch (e: Exception) {
            logger.error("executor registration failed: {}", e.message)
            false
        }
    }

    fun heartbeat(): Boolean {
        return try {
            val status = ExecutorStatus()
            status.executor = config
            status.executions = Executor.getExecutor()!!.getExecutionStatuses()
            http(heartbeatUrl, status)
            logger.info("executor heartbeat succeeded")
            true
        } catch (e: Exception) {
            logger.error("executor heartbeat failed: {}", e.message)
            false
        }
    }

    private fun http(url: String, send: JsonStringify): String {
        val load = send.toJson().toByteArray(StandardCharsets.UTF_8)
        val req = HttpRequest.newBuilder(URI.create(url))
            .POST(HttpRequest.BodyPublishers.ofByteArray(load))
            .header(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
            .build()
        val client = HttpClient.newHttpClient()
        val rsp = client.send(req, HttpResponse.BodyHandlers.ofString())
        if (rsp.statusCode() != HttpResponseStatus.OK.code()) {
            throw CoVaException("Bad dispatcher response: ${rsp.body()}")
        }
        return rsp.body()
    }
}
