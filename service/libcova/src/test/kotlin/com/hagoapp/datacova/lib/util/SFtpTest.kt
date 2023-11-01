/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util

import com.hagoapp.datacova.lib.distribute.conf.SFtpConfig
import com.hagoapp.datacova.lib.util.ssh.KnownHostsStore
import com.jcraft.jsch.SftpException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.util.UUID

class SFtpTest {

    private val logger = LoggerFactory.getLogger(SFtpTest::class.java)

    @Test
    fun testSFtpWithPassword() {
        val config = SFtpConfig()
        config.host = "test.rebex.net"
        config.port = 22
        config.login = "demo"
        config.password = "password"
        SFtpClient(config, KnownHostsStore.MemoryKnownHostStore()).use { it ->
            val sftp = it.getClient()
            val entries = it.list()
            Assertions.assertNotNull(entries)
            val dir = entries.firstOrNull() { entry -> entry.isDirectory && entry.name != "." && entry.name != ".." }
            if (dir != null) {
                sftp.cd(dir.name)
                val pwd = sftp.pwd()
                Assertions.assertEquals("/" + dir.name, pwd)
                sftp.cd("../")
                Assertions.assertEquals("/", sftp.pwd())
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
