/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.JsonStringify
import com.hagoapp.datacova.config.init.CoVaConfig
import com.hagoapp.datacova.config.init.ExecutorConfig
import com.hagoapp.datacova.entity.Executor
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpHeaders
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
    private val logger = CoVaLogger.getLogger()

    fun register(): Boolean {
        val executor = Executor()
        with(executor) {
            name = config.name
            url = config.executorUrl
        }
        return try {
            http(registerUrl, executor)
            logger.info("executor registration succeeded")
            true
        } catch (e: Exception) {
            logger.error("executor registration failed: {}", e.message)
            false
        }
    }

    fun heartbeat(): Boolean {
        val executor = Executor()
        with(executor) {
            name = config.name
            url = config.executorUrl
        }
        return try {
            http(heartbeatUrl, executor)
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
