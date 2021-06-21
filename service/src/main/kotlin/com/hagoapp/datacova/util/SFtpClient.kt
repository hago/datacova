package com.hagoapp.datacova.util

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.InputStream

class SFtpClient(
    server: String,
    serverPort: Int = 22,
    login: String,
    pwd: String,
    knownHosts: InputStream
) {
    private val host = server
    private val port = serverPort
    private val username = login
    private val password = pwd

    private val sshClient: JSch = JSch()
    private var channel: ChannelSftp? = null
    private val session: Session

    init {
        sshClient.setKnownHosts(knownHosts)
        session = sshClient.getSession(username, host, port)
        session.setPassword(password)
    }

    fun getClient(): ChannelSftp {
        if (channel == null) {
            session.connect()
            channel = session.openChannel("sftp") as ChannelSftp
            channel!!.connect()
        }
        return channel!!
    }
}