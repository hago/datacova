/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.distribute

import com.google.gson.Gson
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.distribute.conf.FtpConfig
import com.hagoapp.datacova.util.FtpClient
import com.hagoapp.datacova.util.StackTraceWriter
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext

class Ftp {

    private val logger = CoVaLogger.getLogger()

    @WebEndPoint(
        methods = [MethodName.POST],
        authTypes = [AuthType.UserToken],
        path = "/api/distribute/verify/ftp"
    )
    fun verify(context: RoutingContext) {
        val json = context.body().asString()
        val config = Gson().fromJson(json, FtpConfig::class.java)
        if (config == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "not valid ftp config json")
            return
        }
        logger.debug("ftp config: {}", config.toJson())
        try {
            val pwd = FtpClient(config).use {
                it.pwd()
            }
            logger.debug("ftp session connected, remote path is {}", pwd)
            ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("data" to mapOf("pwd" to pwd)))
        } catch (ex: Exception) {
            logger.error("Ftp verification error: {}", ex.message)
            StackTraceWriter.write(ex, logger)
            ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.message)
        }
    }
}
