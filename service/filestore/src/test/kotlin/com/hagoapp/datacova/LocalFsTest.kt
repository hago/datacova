/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova

import com.hagoapp.datacova.file.localfs.LocalFsFileStore
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import java.util.*
import kotlin.random.Random

/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

class LocalFsTest {

    companion object {

        private lateinit var store: LocalFsFileStore
        private val logger = LoggerFactory.getLogger(LocalFsTest::class.java)
        private lateinit var testRootPath: String
        private val testFileName = "Test测试試験테스트"

        @JvmStatic
        @BeforeAll
        fun initStore() {
            val hay = Random.nextInt(Integer.MAX_VALUE)
            val md5 = MessageDigest.getInstance("md5")
            val rootPath = md5.digest(hay.toString().toByteArray())
                .joinToString("", transform = { aByte -> String.format("%02x", aByte) })
            testRootPath = File(rootPath).absolutePath
            store = LocalFsFileStore.getFileStore(testRootPath)
        }

        @JvmStatic
        @AfterAll()
        fun cleanUp() {
            try {
                File(testRootPath).deleteRecursively()
                logger.debug("LocalFs file store for test is deleted: {}", testRootPath)
            } catch (e: Exception) {
                logger.error("Error while cleanup: {}", e.message)
            }
        }
    }

    @Test
    @Order(1)
    fun testWriteFile() {
        val len = Random.nextInt(1024 * 1024 * 10)
        val data = ByteArray(len).toTypedArray()
        Arrays.setAll(data) { _ -> Random.nextBytes(1)[0] }
        ByteArrayInputStream(data.toByteArray()).use { src ->
            val id = store.putFile(src, testFileName, len.toLong())
            logger.debug("File saved, returned id: {}", id)
        }
    }

}
