/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web.default

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.WebInterface
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpHeaders

class FavIcon : WebInterface {
    private val logger = CoVaLogger.getLogger()

    override fun getPath(): String {
        return "/favicon.ico";
    }

    override fun requestHandlers(): Map<String, WebInterface.Handler> {
        return mapOf(
            MethodName.GET to WebInterface.Handler { context ->
                val stream = FavIcon::class.java.getResourceAsStream("/web/favicon.png")
                if (stream == null) {
                    context.fail(404)
                    return@Handler
                }
                stream.use {
                    val content = it.readAllBytes()
                    ResponseHelper.sendResponse(
                        context, HttpResponseStatus.OK,
                        mutableMapOf(
                            HttpHeaders.CONTENT_TYPE.toString() to "image/png"
                        ),
                        content
                    )
                }
            })
    }
}