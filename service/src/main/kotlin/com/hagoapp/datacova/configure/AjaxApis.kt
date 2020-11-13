package com.hagoapp.datacova.configure

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.DatabaseConnection
import com.hagoapp.datacova.util.http.RequestHelper
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import java.lang.Exception

class AjaxApis {

    private val logger = CoVaLogger.getLogger();

    @WebEndPoint(methods = [HttpMethod.POST], path = "/db/connect", isBlocking = true)
    fun connectDatabase(routingContext: RoutingContext) {
        logger.debug("database config data received: ${RequestHelper.readBodyString(routingContext)}")
        val config = RequestHelper.readBodyClass(routingContext, DatabaseConfig::class.java)
        if (config == null) {
            logger.error("/db/connect got invalid post")
            ResponseHelper.sendResponse(routingContext, HttpResponseStatus.BAD_REQUEST);
        } else {
            var databases: List<String>? = null
            val result = try {
                val connection = DatabaseConnection.getDatabaseConnection(config)
                databases = connection.listDatabases()
                Pair(true, null)
            } catch (e: CoVaException) {
                Pair(false, e.cause!!.message)
            } catch (e: Exception) {
                Pair(false, e.message)
            }
            ResponseHelper.sendResponse(
                routingContext, HttpResponseStatus.OK, mapOf(
                    "code" to 0,
                    "data" to mapOf(
                        "result" to result.first,
                        "detail" to if (result.first) null else result.second,
                        "databases" to databases
                    )
                )
            )
        }
    }
}