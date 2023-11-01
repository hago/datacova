/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.file

import com.hagoapp.datacova.file.localfs.LocalFsFileStore
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.random.Random

/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
@TestMethodOrder(OrderAnnotation::class)
class LocalFsTest {

    companion object {

        private lateinit var store: LocalFsFileStore
        private val logger = LoggerFactory.getLogger(LocalFsTest::class.java)
        private lateinit var testRootPath: String
        private const val testFileName = "Test测试試験테스트"
        private val len = Random.nextInt(1024 * 1024 * 10)
        private val data = ByteArray(len)
        private val rand = SecureRandom()
        private lateinit var id: String

        init {
            rand.nextBytes(data)
        }

        @JvmStatic
        @BeforeAll
        fun initStore() {
            logger.debug("init")
            val hay = rand.nextInt(Integer.MAX_VALUE)
            val md5 = MessageDigest.getInstance("md5")
            val rootPath = md5.digest(hay.toString().toByteArray())
                .joinToString("", transform = { aByte -> String.format("%02x", aByte) })
            testRootPath = File(rootPath).absolutePath
            store = LocalFsFileStore(testRootPath)
        }

        @JvmStatic
        @AfterAll()
        fun cleanUp() {
            logger.debug("cleanup")
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
        logger.debug("write file")
        ByteArrayInputStream(data).use { src ->
            id = store.putFile(src, testFileName, len.toLong())
            logger.debug("File saved, returned id: {}", id)
            //Assertions.assertTrue(store.exists(id))
        }
    }

    @Test
    @Order(2)
    fun testExists() {
        logger.debug("file exists?")
        Assertions.assertTrue(store.exists(id))
    }

    @Test
    @Order(3)
    fun testReadFile() {
        logger.debug("read file")
        val info = store.getFileInfo(id)
        logger.debug("file info: {}", info)
        store.getFile(id).use { stream ->
            {
                val buffer = ByteArray(1)
                for (i in data.indices) {
                    val n = stream.read(buffer, 0, 1)
                    Assertions.assertEquals(1, n)
                    Assertions.assertEquals(data[i], buffer[0])
                }
                Assertions.assertEquals(-1, stream.read())
            }
        }
    }

    @Test
    @Order(4)
    fun testDeleteFile() {
        logger.debug("delete file")
        Assertions.assertTrue(store.delete(id))
    }

    @Test
    @Order(5)
    fun testNotExists() {
        logger.debug("file not exists?")
        Assertions.assertFalse(store.exists(id))
    }
}