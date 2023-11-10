/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility

import com.hagoapp.datacova.file.FileStoreFactory
import com.hagoapp.datacova.utility.filestore.RedisFileStore
import com.hagoapp.datacova.utility.filestore.RedisFsConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class RedisFsTest {

    @Test
    fun testCreateRedisFsStoreViaFactory() {
        val fsConfig = RedisFsConfig()
        fsConfig.name = "TEST"
        fsConfig.config = RedisTestConstants.createRedisConfig()
        val store = FileStoreFactory.createFileStore(fsConfig)
        Assertions.assertNotNull(store)
        Assertions.assertTrue(store is RedisFileStore)
    }

    @Test
    fun testRedisFileStore() {
        val fsConfig = RedisFsConfig()
        fsConfig.name = "TEST"
        fsConfig.config = RedisTestConstants.createRedisConfig()
        val fs = RedisFileStore(fsConfig)
        val fileName = "test"
        val len = 1024 * 1024
        val fileData = ByteArray(len) { Random.nextInt(255).toUByte().toByte() }
        val id = ByteArrayInputStream(fileData).use {
            fs.putFile(it, fileName, len.toLong())
        }
        Assertions.assertTrue(fs.exists(id))
        val retrieved = fs.getFile(id).use { src ->
            ByteArrayOutputStream().use {
                val buffer = ByteArray(Random.nextInt(1024, len))
                while (true) {
                    val i = src.read(buffer, 0, buffer.size)
                    if (i < 0) {
                        break
                    }
                    it.write(buffer, 0, i)
                }
                it.toByteArray()
            }
        }
        Assertions.assertEquals(fileData.size, retrieved.size)
        Assertions.assertTrue(fileData.indices.all { fileData[it] == retrieved[it] })
        val info = fs.getFileInfo(id)
        Assertions.assertEquals(fileName, info.originalFileName)
        Assertions.assertEquals(len.toLong(), info.size)
        fs.delete(id)
        Assertions.assertFalse(fs.exists(id))
    }
}
