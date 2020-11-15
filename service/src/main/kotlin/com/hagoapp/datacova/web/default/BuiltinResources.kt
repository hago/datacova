package com.hagoapp.datacova.web.default

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.web.annotation.WebEndPoint
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpHeaders
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class BuiltinResources {

    @WebEndPoint(methods = [HttpMethod.GET, HttpMethod.POST], path = "/resources/.+", isPathRegex = true)
    fun getResource(context: RoutingContext) {
        val path = context.request().path()
        val localPath = path.replaceFirst("resources", "web", true)
        val conn = BuiltinResources::class.java.getResource(localPath)?.toURI()?.toURL()?.openConnection()
        if (conn == null) {
            context.fail(404, CoVaException("$path not found"))
            return
        }
        val contentType = conn.contentType ?: "application/octet-stream"
        val stream = BuiltinResources::class.java.getResourceAsStream(localPath)
        if (stream == null) {
            context.fail(404, CoVaException("$path not found"))
        }
        stream.use {
            val content = it.readAllBytes()
            context.response().putHeader(HttpHeaders.CONTENT_TYPE, contentType)
            context.response().putHeader(HttpHeaders.CONTENT_LENGTH, content.size.toString())
            context.response().write(Buffer.buffer(content))
        }
    }

}