/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.configure

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.WebInterface
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod

class PageDisplay : WebInterface {

    private val logger = CoVaLogger.getLogger()

    override fun getPath(): String {
        return "/";
    }

    override fun requestHandlers(): Map<HttpMethod, WebInterface.Handler> {
        return mapOf(
            HttpMethod.GET to WebInterface.Handler { context ->
                PageDisplay::class.java.getResourceAsStream("/configure.html").use {
                    ResponseHelper.sendResponse(
                        context, HttpResponseStatus.OK,
                        mapOf("Content-Type" to "text/html"), it.readAllBytes()
                    )
                }
            }
        )
    }
}