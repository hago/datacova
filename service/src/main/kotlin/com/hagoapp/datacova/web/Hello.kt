package com.hagoapp.datacova.web

import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class Hello {

    @WebEndPoint(methods = [HttpMethod.GET], path = "/hello", isBlocking = false)
    fun hello(routingContext: RoutingContext) {
        ResponseHelper.sendResponse(
            routingContext, HttpResponseStatus.OK,
            mapOf("Content-Type" to "text/plain"), "Hello!".toByteArray()
        )
    }

}