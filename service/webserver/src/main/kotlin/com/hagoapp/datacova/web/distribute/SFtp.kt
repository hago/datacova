/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.distribute

import com.google.gson.Gson
import com.hagoapp.datacova.lib.distribute.conf.SFtpConfig
import com.hagoapp.datacova.lib.distribute.sftp.SFtpAuthType
import com.hagoapp.datacova.lib.util.SFtpClient
import com.hagoapp.datacova.lib.util.ssh.KnownHostsStore
import com.hagoapp.datacova.util.StackTraceWriter
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import java.io.FileInputStream

class SFtp {

    private val logger = LoggerFactory.getLogger(SFtp::class.java)

    @WebEndPoint(
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN],
        path = "/api/distribute/verify/sftp"
    )
    fun verify(context: RoutingContext) {
        val json = context.body().asString()
        val config = Gson().fromJson(json, SFtpConfig::class.java)
        if ((config == null) || (config.authType != SFtpAuthType.PASSWORD)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "not valid sftp config json")
            return
        }
        logger.debug("ssh config: {}", config.toJson())
        try {
            val pwd = SFtpClient(config, KnownHostsStore.MemoryKnownHostStore()).use {
                it.pwd()
            }
            logger.debug("sftp session connected, remote path is {}", pwd)
            ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("data" to mapOf("pwd" to pwd)))
        } catch (ex: Exception) {
            logger.error("SFtp error: {}", ex.message)
            StackTraceWriter.write(ex, logger)
            ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.message)
        }
    }

    @WebEndPoint(
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN],
        path = "/api/distribute/verify/sftp/keyfile"
    )
    fun verifyKeyAuth(context: RoutingContext) {
        if (context.fileUploads().isEmpty()) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "no key file found")
            return
        }
        val json = context.request().getParam("config")
        val config = Gson().fromJson(json, SFtpConfig::class.java)
        if ((config == null) || (config.authType != SFtpAuthType.PRIVATE_KEY)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "not valid sftp config json")
            return
        }
        logger.debug("ssh config: {}", config.toJson())
        val f = context.fileUploads().first()
        val key = FileInputStream(f.uploadedFileName()).use {
            it.readAllBytes()
        }
        config.privateKey = key
        logger.debug("private key file is {}", config.privateKeyFile)
        try {
            val pwd = SFtpClient(config, KnownHostsStore.MemoryKnownHostStore()).use {
                it.pwd()
            }
            logger.debug("sftp session connected, remote path is {}", pwd)
            ResponseHelper.sendResponse(
                context, HttpResponseStatus.OK, mapOf("code" to 0, "data" to mapOf("keyStored" to null, "pwd" to pwd))
            )
        } catch (ex: Exception) {
            logger.error("SFtp error: {}", ex.message)
            StackTraceWriter.write(ex, logger)
            ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.message)
        }
    }
}
