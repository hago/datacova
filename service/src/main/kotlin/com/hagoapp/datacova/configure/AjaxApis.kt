/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.configure

import com.google.gson.GsonBuilder
import com.hagoapp.datacova.Application
import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.command.Configure
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.util.http.RequestHelper
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.f2t.database.DbConnectionFactory
import com.hagoapp.f2t.database.config.DbConfigReader
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.charset.StandardCharsets

class AjaxApis {

    private val logger = CoVaLogger.getLogger();

    @WebEndPoint(methods = [HttpMethod.POST], path = "/db/connect", isBlocking = true)
    fun connectDatabase(routingContext: RoutingContext) {
        logger.debug("database config data received: ${RequestHelper.readBodyString(routingContext)}")
        val json = RequestHelper.readBodyString(routingContext)
        val config = DbConfigReader.json2DbConfig(json)
        if (config == null) {
            logger.error("/db/connect got invalid post")
            ResponseHelper.sendResponse(routingContext, HttpResponseStatus.BAD_REQUEST)
        } else {
            var databases: List<String>? = null
            val result = try {
                val connection = DbConnectionFactory.createDbConnection(config)
                databases = connection.listDatabases(config)
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

    @WebEndPoint(methods = [HttpMethod.POST], path = "/config/save")
    fun generateConfigFile(context: RoutingContext) {
        logger.debug("database config data received: ${RequestHelper.readBodyString(context)}")
        val config = RequestHelper.readBodyClass(context, CoVaConfig::class.java)
        if (config == null) {
            logger.error("/config/save got invalid post")
            ResponseHelper.sendResponse(context, HttpResponseStatus.BAD_REQUEST)
        } else {
            when (val filename = Application.getData(Configure.CONFIG_FILE_TO_WRITE)) {
                null -> {
                    context.fail(HttpResponseStatus.BAD_REQUEST.code(), CoVaException("file name null"))
                }
                else -> {
                    try {
                        val stream = FileOutputStream(filename.toString())
                        stream.use {
                            val json = GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(config)
                            stream.write(json.toByteArray(StandardCharsets.UTF_8))
                            logger.info("file $filename created")
                        }
                        ResponseHelper.sendResponse(context, HttpResponseStatus.OK);
                    } catch (e: IOException) {
                        context.fail(
                            HttpResponseStatus.INTERNAL_SERVER_ERROR.code(),
                            CoVaException("/config/save create $filename failed")
                        )
                    }
                }
            }
        }
    }
}