/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.random.Random

class MessageIoTest {

    private val cases = listOf<Any>(
        RegisterMessage("regular", "key", null),
        RegisterMessage("regular", "key", "worker demo"),
        RegisterResponseMessage(true, UUID.randomUUID().toString()),
        RegisterResponseMessage(false, UUID.randomUUID().toString()),
        TaskExecutionMessage("", mapOf(1 to "")),
        HeartBeatMessage(Instant.now().toEpochMilli(), randomString(16)),
        HeartBeatResponseMessage(Instant.now().toEpochMilli(), randomString(19)),
        WorkerDoneMessage(randomString(10), randomString(20))
    )

    private fun randomString(length: Int): String {
        val random = Random(Instant.now().toEpochMilli())
        return IntRange(0, length).map { _ -> Char(random.nextInt(10, 127)) }.joinToString("")
    }

    @Test
    fun testMessageReadWrite() {
        for (c in cases) {
            val message = MessageWriter.toBytes(c)
            Assertions.assertNotNull(message)
            val data = MessageReader().use {
                it.update(message)
                it.parseMessage()
            }
            Assertions.assertNotNull(data)
            Assertions.assertEquals(c, data)
        }
    }

    @Test
    fun testMessageReadWrite2() {
        for (c in cases) {
            val message = MessageWriter.toBytes(c)
            Assertions.assertNotNull(message)
            val data = MessageReader.readMessage(message)
            Assertions.assertNotNull(data)
            Assertions.assertEquals(c, data)
        }
    }
}
