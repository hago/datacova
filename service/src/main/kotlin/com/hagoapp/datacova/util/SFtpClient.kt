package com.hagoapp.datacova.util

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.execution.distribute.sftp.KnownHostsStore
import com.jcraft.jsch.*
import java.io.Closeable

class SFtpClient(
    private val host: String,
    private val port: Int = 22,
    private val login: String,
    private val password: String,
    private val knownHostsStore: KnownHostsStore
) : Closeable {
    private val sshClient: JSch = JSch()
    private var channel: ChannelSftp? = null
    private val session: Session
    private val logger = CoVaLogger.getLogger()

    init {
        knownHostsStore.toStream().use { sshClient.setKnownHosts(it) }
        session = connectSession()
        session.setPassword(password)
    }

    fun getClient(): ChannelSftp {
        if (channel == null) {
            channel = session.openChannel("sftp") as ChannelSftp
            channel!!.connect()
        }
        return channel!!
    }


    private fun connectSession(): Session {
        val session = createSession()
        return try {
            session.connect()
            session
        } catch (ex: JSchException) {
            logger.error("connect ssh session to {}:{} error: {}", host, port, ex.message)
            if (ex.toString().indexOf("UnknownHostKey", 0, true) >= 0) {
                logger.info("connect ssh session fail for unknown host {}:{}, add key and retry", host, port)
                updateKnownHosts(session.hostKey)
                knownHostsStore.toStream().use { sshClient.setKnownHosts(it) }
                val session1 = createSession()
                session1.connect()
                logger.info("reconnect ssh session fail for {}:{} successfully", host, port)
                session1
            } else throw ex
        }
    }

    private fun createSession(): Session {
        sshClient.setKnownHosts(knownHostsStore.toStream())
        val session = sshClient.getSession(login, host, port)
        session.setPassword(password)
        return session
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
            session.disconnect()
        } catch (ignored: Throwable) {
            //
        }
    }
}