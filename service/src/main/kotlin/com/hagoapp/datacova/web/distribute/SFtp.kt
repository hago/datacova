/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.distribute

import com.google.gson.Gson
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.entity.action.distribute.conf.SFtpConfig
import com.hagoapp.datacova.execution.distribute.sftp.KnownHostsStore
import com.hagoapp.datacova.util.SFtpClient
import com.hagoapp.datacova.util.StackTraceWriter
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.jcraft.jsch.*
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class SFtp {

    private val logger = CoVaLogger.getLogger()

    @WebEndPoint(
        methods = [HttpMethod.POST],
        authTypes = [AuthType.UserToken],
        path = "/api/distribute/verify/sftp"
    )
    fun verify(context: RoutingContext) {
        val json = context.bodyAsString
        val config = Gson().fromJson(json, SFtpConfig::class.java)
        if (config == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "not valid sftp config json")
            return
        }
        logger.debug("ssh config: {}", config.toJson())
        try {
            SFtpClient(
                config.host, config.port, config.login, config.password,
                KnownHostsStore.getStore()
            ).use {
                val ftp = it.getClient()
                if ((config.remotePath != null) && config.remotePath.isNotBlank()) {
                    ftp.cd(config.remotePath)
                } else {
                    config.remotePath = ftp.pwd()
                }
                logger.debug("sftp session connected, remote path is {}", config.remotePath)
            }
        } catch (ex: JSchException) {
            logger.error("JSchException error: {}", ex.message)
            StackTraceWriter.write(ex, logger)
            ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.message)
        } catch (ex: SftpException) {
            logger.error("SftpException error: {}", ex.message)
            StackTraceWriter.write(ex, logger)
            ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.message)
        }
    }
}
