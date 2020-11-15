package com.hagoapp.datacova.configure

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.WebInterface
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod

class FavIcon : WebInterface {
    private val logger = CoVaLogger.getLogger()

    override fun getPath(): String {
        return "/favicon.ico";
    }

    override fun requestHandlers(): Map<HttpMethod, WebInterface.Handler> {
        return mapOf(
            HttpMethod.GET to WebInterface.Handler { context ->
                val stream = PageDisplay::class.java.getResourceAsStream("/web/favicon.ico")
                if (stream == null) {
                    context.fail(404)
                } else {
                    stream.use {
                        val content = it.readAllBytes()
                        ResponseHelper.sendResponse(
                            context, HttpResponseStatus.OK,
                            mutableMapOf(
                                HttpHeaders.CONTENT_TYPE.toString() to "text/html"
                            ),
                            content
                        )
                    }
                }
            })
    }
}