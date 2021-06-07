/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.dispatcher

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.Executor
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpHeaders
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * This class send a task execution to an executor to run.
 */
class ExecutorInvoker(val executor: Executor) {

    private val executorUrl = "${executor.url}/api/executor/execute"

    fun dispatch(executionId: Int) {
        val req = HttpRequest.newBuilder(URI.create("$executorUrl/$executionId"))
            .POST(HttpRequest.BodyPublishers.ofByteArray(ByteArray(0)))
            .header(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
            .header(HttpHeaders.CONTENT_LENGTH.toString(), "0")
            .build()
        val client = HttpClient.newHttpClient()
        val rsp = client.send(req, HttpResponse.BodyHandlers.ofString())
        val code = rsp.statusCode()
        if (code != HttpResponseStatus.OK.code()) {
            throw CoVaException("error occurs when send execution to executor ${executor.name}: ${rsp.body()}")
        }
    }
}
