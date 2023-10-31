package com.hagoapp.datacova.util

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.distribute.conf.SFtpConfig
import com.hagoapp.datacova.entity.distribute.sftp.SFtpAuthType
import com.hagoapp.datacova.util.ssh.KnownHostsStore
import com.jcraft.jsch.*
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

class SFtpClient(
    private val config: SFtpConfig,
    private val knownHostsStore: KnownHostsStore
) : Closeable {

    companion object {
        fun isValidPrivateKeyFile(filename: String): Boolean {
            val content = FileInputStream(filename).use {
                it.readAllBytes()
            }.toString(StandardCharsets.UTF_8).lowercase()
            return content.contains("begin rsa private key")
        }

        fun createPemFileName(login: String, host: String, port: Int): String {
            return "$login/$host-$port.pem"
        }
    }

    private val sshClient: JSch = JSch()
    private var channel: ChannelSftp? = null
    private var session: Session? = null
    private val logger = LoggerFactory.getLogger(SFtpClient::class.java)

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
        return if (config.authType == SFtpAuthType.PRIVATE_KEY) {
            if (config.privateKeyFile == null) {
                throw CoVaException("private key file for ${config.host}:${config.port} is missing")
            }
            val actualKeyFile = config.privateKeyFile
            val phrase = when {
                config.passPhrase == null -> null
                config.passPhrase.isEmpty() -> null
                else -> config.passPhrase
            }
            sshClient.addIdentity(actualKeyFile, phrase)
            sshClient.getSession(config.login, config.host, config.port)
        } else {
            val session = sshClient.getSession(config.login, config.host, config.port)
            session.setPassword(config.password)
            session
        }
    }

    private fun updateKnownHosts(hostKey: HostKey) {
        knownHostsStore.updateHost(hostKey)
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
}