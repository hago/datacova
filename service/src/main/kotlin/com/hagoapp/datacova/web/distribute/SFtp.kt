/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.distribute

import com.google.gson.Gson
import com.hagoapp.datacova.entity.action.distribute.conf.SFtpConfig
import com.hagoapp.datacova.execution.distribute.sftp.KnownHostsStore
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.jcraft.jsch.*
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class SFtp {
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
        try {
            val session = connectSession(config)
            val channel = session.openChannel("sftp") as ChannelSftp
            channel.connect()
            channel.cd(config.remotePath)
        } catch (ex: JSchException) {
            ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.message)
        } catch (ex: SftpException) {
            ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, ex.message)
        }
    }

    private fun connectSession(config: SFtpConfig): Session {
        val session = createSession(config)
        return try {
            session.connect()
            session
        } catch (ex: JSchException) {
            if (ex.toString().indexOf("UnknownHostKey", 0, true) >= 0) {
                updateKnownHosts(session.hostKey)
                val session1 = createSession(config)
                session1.connect()
                session1
            } else throw ex
        }
    }

    private fun createSession(config: SFtpConfig): Session {
        val client = JSch()
        KnownHostsStore.getStore().toStream().use { client.setKnownHosts(it) }
        val session = client.getSession(config.login)
        session.host = config.host
        session.port = config.port
        session.setPassword(config.password)
        return session
    }

    private fun updateKnownHosts(hostKey: HostKey) {
//        println("marker: ${hostKey.marker}")
//        println("host: ${hostKey.host}")
//        println("comment: ${hostKey.comment}")
//        println("key: ${hostKey.key}")
//        println("type: ${hostKey.type}")
        KnownHostsStore.getStore().updateHost(hostKey)
    }
}
