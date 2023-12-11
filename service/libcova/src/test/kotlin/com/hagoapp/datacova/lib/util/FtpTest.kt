/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util

import com.hagoapp.datacova.lib.distribute.conf.FtpConfig
import com.hagoapp.datacova.lib.util.FtpClient
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.util.UUID

class FtpTest {

    private val config: FtpConfig = FtpConfig()

    init {
        config.host = "ftp.dlptest.com"
        config.port = 21
        config.login = "dlpuser"
        config.password = "rNrKYTX9g7z3RgJRmxWuGHbeu"
    }

    @Test
    fun testFtp() {
        FtpClient(config).use { it ->
            val entries = it.list()
            Assertions.assertNotNull(entries)
            val dir = entries.firstOrNull() { entry -> entry.isDirectory }
            if (dir != null) {
                it.cd(dir.name)
                val pwd = it.pwd()
                Assertions.assertEquals("/" + dir.name, pwd)
                it.cd("../")
                Assertions.assertEquals("/", it.pwd())
            }
            val fn = UUID.randomUUID().toString()
            it.put(fn, fn.toByteArray())
            val content = String(it.get(fn))
            Assertions.assertEquals(fn, content)
            it.delete(fn)
        }
    }

    @Test
    fun testFtpStreamSource() {
        FtpClient(config).use { it ->
            val entries = it.list()
            Assertions.assertNotNull(entries)
            val dir = entries.firstOrNull() { entry -> entry.isDirectory }
            if (dir != null) {
                it.cd(dir.name)
                val pwd = it.pwd()
                Assertions.assertEquals("/" + dir.name, pwd)
                it.cd("../")
                Assertions.assertEquals("/", it.pwd())
            }
            val fn = UUID.randomUUID().toString()
            ByteArrayInputStream(fn.toByteArray()).use { stream ->
                it.put(fn, stream)
            }
            val content = String(it.get(fn))
            Assertions.assertEquals(fn, content)
            it.delete(fn)
        }
    }
}
