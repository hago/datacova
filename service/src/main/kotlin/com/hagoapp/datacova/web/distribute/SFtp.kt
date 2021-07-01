/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.distribute

import com.google.gson.Gson
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.distribute.sftp.SFtpAuthType
import com.hagoapp.datacova.entity.action.distribute.conf.SFtpConfig
import com.hagoapp.datacova.execution.distribute.sftp.KnownHostsStore
import com.hagoapp.datacova.util.FileStoreUtils
import com.hagoapp.datacova.util.SFtpClient
import com.hagoapp.datacova.util.StackTraceWriter
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.jcraft.jsch.*
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import java.io.FileInputStream

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
        if ((config == null) || (config.authType != SFtpAuthType.Password)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "not valid sftp config json")
            return
        }
        logger.debug("ssh config: {}", config.toJson())
        try {
            val pwd = SFtpClient(config, KnownHostsStore.getStore()).use {
                val ftp = it.getClient()
                ftp.pwd()
            }
            logger.debug("sftp session connected, remote path is {}", pwd)
            ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("data" to mapOf("pwd" to pwd)))
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

    @WebEndPoint(
        methods = [HttpMethod.POST],
        authTypes = [AuthType.UserToken],
        path = "/api/distribute/verify/sftp/keyfile"
    )
    fun verifyKeyAuth(context: RoutingContext) {
        if (context.fileUploads().isEmpty()) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "no key file found")
            return
        }
        val json = context.request().getParam("config")
        val config = Gson().fromJson(json, SFtpConfig::class.java)
        if ((config == null) || (config.authType != SFtpAuthType.PrivateKey)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "not valid sftp config json")
            return
        }
        logger.debug("ssh config: {}", config.toJson())
        val f = context.fileUploads().first()
        val target = SFtpClient.createPemFileName(config.login, config.host, config.port)
        val fs = FileStoreUtils.getSshFileStore()
        FileInputStream(f.uploadedFileName()).use {
            fs.saveFileToStore(target, it)
        }
        val keyFullName = fs.getFullFileName(target)
        if (!SFtpClient.isValidPrivateKeyFile(keyFullName)) {
            ResponseHelper.respondError(
                context,
                HttpResponseStatus.BAD_REQUEST,
                "Acceptable private key file should start with 'BEGIN RSA PRIVATE KEY'"
            )
            return
        }
        config.privateKeyFile = keyFullName
        try {
            val pwd = SFtpClient(config, KnownHostsStore.getStore()).use {
                val ftp = it.getClient()
                ftp.pwd()
            }
            logger.debug("sftp session connected, remote path is {}", pwd)
            ResponseHelper.sendResponse(
                context, HttpResponseStatus.OK, mapOf("code" to 0, "data" to mapOf("keyStored" to target, "pwd" to pwd))
            )
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
