package com.hagoapp.datacova.util

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.distribute.sftp.SFtpAuthType
import com.hagoapp.datacova.entity.action.distribute.conf.SFtpConfig
import com.hagoapp.datacova.execution.distribute.sftp.KnownHostsStore
import com.jcraft.jsch.*
import java.io.Closeable
import java.io.File

class SFtpClient(
    private val config: SFtpConfig,
    private val knownHostsStore: KnownHostsStore
) : Closeable {
    private val sshClient: JSch = JSch()
    private var channel: ChannelSftp? = null
    private var session: Session? = null
    private val logger = CoVaLogger.getLogger()

    init {
        knownHostsStore.toStream().use { sshClient.setKnownHosts(it) }
        session = createSession()
    }

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
                updateKnownHosts(session!!.hostKey)
                session!!.disconnect()
                knownHostsStore.toStream().use { sshClient.setKnownHosts(it) }
                session = createSession()
                session!!.connect()
                logger.info("reconnect ssh session fail for {}:{} successfully", config.host, config.port)
                session!!
            } else throw ex
        }
    }

    private fun createSession(): Session {
        sshClient.setKnownHosts(knownHostsStore.toStream())
        if ((config.password == null) && (config.privateKeyFile == null)) {
            throw CoVaException("Credential is missing for sftp ${config.host}:${config.port}")
        }
        return if (config.authType == SFtpAuthType.PrivateKey) {
            if ((config.privateKeyFile == null) || !File(config.privateKeyFile).exists()) {
                throw CoVaException("private key file for ${config.host}:${config.port} is missing")
            }
            val phrase = when {
                config.passPhrase == null -> null
                config.passPhrase.isEmpty() -> null
                else -> config.passPhrase
            }
            sshClient.addIdentity(config.privateKeyFile, phrase)
            sshClient.getSession(config.login, config.host, config.port)
        } else {
            val session = sshClient.getSession(config.login, config.host, config.port)
            session.setPassword(config.password)
            session
        }
    }

    private fun updateKnownHosts(hostKey: HostKey) {
//        println("marker: ${hostKey.marker}")
//        println("host: ${hostKey.host}")
//        println("comment: ${hostKey.comment}")
//        println("key: ${hostKey.key}")
//        println("type: ${hostKey.type}")
        knownHostsStore.updateHost(hostKey)
    }

    override fun close() {
        try {
            channel?.disconnect()
        } catch (ignored: Throwable) {
            //
        }
        try {
            session?.disconnect()
        } catch (ignored: Throwable) {
            //
        }
    }
}