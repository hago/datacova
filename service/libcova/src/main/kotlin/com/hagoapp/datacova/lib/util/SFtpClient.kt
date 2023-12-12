/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util

import com.hagoapp.datacova.utility.CoVaException
import com.hagoapp.datacova.lib.distribute.conf.SFtpConfig
import com.hagoapp.datacova.lib.distribute.sftp.SFtpAuthType
import com.hagoapp.datacova.lib.ssh.HostKeyItem
import com.hagoapp.datacova.lib.util.ssh.KnownHostsStore
import com.jcraft.jsch.*
import com.jcraft.jsch.ChannelSftp.LsEntrySelector
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.InputStream

class SFtpClient(
    private val config: SFtpConfig,
    private val knownHostsStore: KnownHostsStore
) : Closeable {

    private val sshClient: JSch = JSch()
    private var channel: ChannelSftp? = null
    private var session: Session? = null
    private val logger = LoggerFactory.getLogger(SFtpClient::class.java)

    fun getClient(): ChannelSftp {
        if (channel == null) {
            val session = connectSession()
            channel = session.openChannel("sftp") as ChannelSftp
            channel!!.connect()
        }
        return channel!!
    }

    private fun connectSession(): Session {
        session = createSession()
        return try {
            session!!.connect()
            session!!
        } catch (ex: JSchException) {
            logger.error("connect ssh session to {}:{} error: {}", config.host, config.port, ex.message)
            if (ex.toString().indexOf("UnknownHostKey", 0, true) >= 0) {
                logger.info(
                    "connect ssh session fail for unknown host {}:{}, add key and retry",
                    config.host,
                    config.port
                )
                knownHostsStore.update(
                    HostKeyItem(
                        session!!.hostKey.host,
                        session!!.hostKey.type,
                        session!!.hostKey.key
                    )
                )
                session!!.disconnect()
                session = createSession()
                session!!.connect()
                logger.info("reconnect ssh session fail for {}:{} successfully", config.host, config.port)
                session!!
            } else throw ex
        }
    }

    private fun createSession(): Session {
        knownHostsStore.toStream().use { sshClient.setKnownHosts(it) }
        if ((config.password == null) && (config.privateKeyFile == null) && (config.privateKey == null)) {
            throw CoVaException("Credential is missing for sftp ${config.host}:${config.port}")
        }
        return if (config.authType == SFtpAuthType.PRIVATE_KEY) {
            val phrase = when {
                config.passPhrase == null -> null
                config.passPhrase.isEmpty() -> null
                else -> config.passPhrase
            }
            if (config.privateKeyFile != null) {
                logger.debug("key file: {}", config.privateKeyFile)
                sshClient.addIdentity(config.privateKeyFile, phrase)
                sshClient.getSession(config.login, config.host, config.port)
            } else if (config.privateKey != null) {
                logger.debug("key {}", config.privateKey)
                sshClient.addIdentity(
                    "identity_${config.host}",
                    config.privateKey,
                    null,
                    phrase?.toByteArray()
                )
                sshClient.getSession(config.login, config.host, config.port)
            } else {
                throw CoVaException("private key file for ${config.host}:${config.port} is missing")
            }
        } else {
            val session = sshClient.getSession(config.login, config.host, config.port)
            session.setPassword(config.password)
            session
        }
    }

    override fun close() {
        try {
            channel?.disconnect()
        } catch (ex: Exception) {
            logger.error("close ssh channel error: {}, ignored", ex.message)
        }
        try {
            session?.disconnect()
        } catch (ex: Exception) {
            logger.error("disconnect ssh session error: {}, ignored", ex.message)
        }
    }

    fun list(path: String? = null): List<RemoteFile> {
        val sftp = getClient()
        val p = path ?: sftp.pwd()
        val ret = mutableListOf<RemoteFile>()
        sftp.ls(p) { entry ->
            val attrs = entry.attrs
            ret.add(RemoteFile(entry.filename, attrs.isDir, attrs.isLink, attrs.size, p))
            LsEntrySelector.CONTINUE
        }
        return ret
    }

    fun cd(path: String): Boolean {
        return try {
            val sftp = getClient()
            sftp.cd(path)
            true
        } catch (ex: SftpException) {
            false
        }
    }

    fun home(): String {
        return getClient().home
    }

    fun pwd(): String {
        return getClient().pwd()
    }

    fun rm(path: String) {
        val sftp = getClient()
        sftp.rm(path)
    }

    fun put(src: InputStream, dest: String) {
        val sftp = getClient()
        sftp.put(src, dest)
    }
}
