/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util

import com.hagoapp.datacova.lib.distribute.conf.SFtpConfig
import com.hagoapp.datacova.lib.distribute.sftp.SFtpAuthType
import com.hagoapp.datacova.lib.util.ssh.KnownHostsStore
import com.jcraft.jsch.SftpException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperties
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.util.UUID

class SFtpTest {

    companion object {
        const val SSH_HOST = "ssh.host"
        const val SSH_PORT = "ssh.port"
        const val SSH_LOGIN = "ssh.login"
        const val SSH_PASSWORD = "ssh.password"
        const val SSH_KEY_FILE = "ssh.keyfile"
        const val SSH_PHRASE = "ssh.phrase"
    }

    private val logger = LoggerFactory.getLogger(SFtpTest::class.java)

    @Test
    fun testSFtpWithPassword() {
        val config = SFtpConfig()
        config.host = System.getProperty(SSH_HOST, "test.rebex.net")
        config.port = System.getProperty(SSH_PORT, "22").toInt()
        config.login = System.getProperty(SSH_LOGIN, "demo")
        config.password = System.getProperty(SSH_PASSWORD, "password")
        config.authType = SFtpAuthType.PASSWORD
        SFtpClient(config, KnownHostsStore.MemoryKnownHostStore()).use { it ->
            val sftp = it.getClient()
            val entries = it.list()
            Assertions.assertNotNull(entries)
            val dir = entries.firstOrNull() { entry -> entry.isDirectory && entry.name != "." && entry.name != ".." }
            if (dir != null) {
                val defaultPath = sftp.realpath("./")
                val expectAfterCd = sftp.realpath(dir.name)
                sftp.cd(dir.name)
                val pwd = sftp.realpath(sftp.pwd())
                Assertions.assertEquals(expectAfterCd, pwd)
                sftp.cd("../")
                Assertions.assertEquals(defaultPath, sftp.realpath(sftp.pwd()))
            }
            try {
                val fn = UUID.randomUUID().toString()
                ByteArrayInputStream(fn.toByteArray()).use { src ->
                    sftp.put(src, fn)
                }
                sftp.get(fn).use { src ->
                    val content = String(src.readAllBytes())
                    Assertions.assertEquals(fn, content)
                }
                sftp.rm(fn)
            } catch (ex: SftpException) {
                logger.warn("Sftp Exception {} while putting file", ex.message)
            }
        }
    }

    @EnabledIfSystemProperties(
        EnabledIfSystemProperty(named = SSH_HOST, matches = ".+"),
        EnabledIfSystemProperty(named = SSH_LOGIN, matches = ".+"),
        EnabledIfSystemProperty(named = SSH_KEY_FILE, matches = ".+")
    )
    @Test
    fun testSFtpWithPrivateKeyFile() {
        val config = SFtpConfig()
        config.host = System.getProperty(SSH_HOST)
        config.port = System.getProperty(SSH_PORT, "22").toInt()
        config.login = System.getProperty(SSH_LOGIN)
        config.authType = SFtpAuthType.PRIVATE_KEY
        config.privateKeyFile = System.getProperty(SSH_KEY_FILE)
        config.passPhrase = System.getProperty(SSH_PHRASE)
        SFtpClient(config, KnownHostsStore.MemoryKnownHostStore()).use { it ->
            val sftp = it.getClient()
            val entries = it.list()
            Assertions.assertNotNull(entries)
            val dir = entries.firstOrNull() { entry ->
                entry.isDirectory && !entry.name.endsWith("..") && !entry.name.endsWith(".")
            }
            if (dir != null) {
                val defaultPath = sftp.realpath("./")
                val expectAfterCd = sftp.realpath(dir.name)
                sftp.cd(dir.name)
                val pwd = sftp.realpath(sftp.pwd())
                Assertions.assertEquals(expectAfterCd, pwd)
                sftp.cd("../")
                Assertions.assertEquals(defaultPath, sftp.realpath(sftp.pwd()))
            }
            try {
                val fn = UUID.randomUUID().toString()
                ByteArrayInputStream(fn.toByteArray()).use { src ->
                    sftp.put(src, fn)
                }
                sftp.get(fn).use { src ->
                    val content = String(src.readAllBytes())
                    Assertions.assertEquals(fn, content)
                }
                sftp.rm(fn)
            } catch (ex: SftpException) {
                logger.warn("Sftp Exception {} while putting file", ex.message)
            }
        }
    }

    @EnabledIfSystemProperties(
        EnabledIfSystemProperty(named = SSH_HOST, matches = ".+"),
        EnabledIfSystemProperty(named = SSH_LOGIN, matches = ".+"),
        EnabledIfSystemProperty(named = SSH_KEY_FILE, matches = ".+")
    )
    @Test
    fun testSFtpWithPrivateKey() {
        val config = SFtpConfig()
        config.host = System.getProperty(SSH_HOST)
        config.port = System.getProperty(SSH_PORT, "22").toInt()
        config.login = System.getProperty(SSH_LOGIN)
        config.authType = SFtpAuthType.PRIVATE_KEY
        FileInputStream(System.getProperty(SSH_KEY_FILE)).use {
            config.privateKey = it.readAllBytes()
        }
        config.passPhrase = System.getProperty(SSH_PHRASE)
        SFtpClient(config, KnownHostsStore.MemoryKnownHostStore()).use { it ->
            val sftp = it.getClient()
            val entries = it.list()
            Assertions.assertNotNull(entries)
            val dir = entries.firstOrNull() { entry ->
                entry.isDirectory && !entry.name.endsWith("..") && !entry.name.endsWith(".")
            }
            if (dir != null) {
                val defaultPath = sftp.realpath("./")
                val expectAfterCd = sftp.realpath(dir.name)
                sftp.cd(dir.name)
                val pwd = sftp.realpath(sftp.pwd())
                Assertions.assertEquals(expectAfterCd, pwd)
                sftp.cd("../")
                Assertions.assertEquals(defaultPath, sftp.realpath(sftp.pwd()))
            }
            try {
                val fn = UUID.randomUUID().toString()
                ByteArrayInputStream(fn.toByteArray()).use { src ->
                    sftp.put(src, fn)
                }
                sftp.get(fn).use { src ->
                    val content = String(src.readAllBytes())
                    Assertions.assertEquals(fn, content)
                }
                sftp.rm(fn)
            } catch (ex: SftpException) {
                logger.warn("Sftp Exception {} while putting file", ex.message)
            }
        }
    }
}
