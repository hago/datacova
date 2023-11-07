/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.utility

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class ByteOpsTest {

    private val logger = LoggerFactory.getLogger(ByteOpsTest::class.java)

    private val bytes2IntCases = listOf(
        Pair(ByteArray(4) { arrayOf(0xff, 0xff, 0xff, 0xff)[it].toByte() }, -1),
        Pair(ByteArray(4) { arrayOf(0x80, 0x00, 0x00, 0x00)[it].toByte() }, Int.MIN_VALUE),
        Pair(ByteArray(4) { arrayOf(0x7f, 0xff, 0xff, 0xff)[it].toByte() }, Int.MAX_VALUE),
        Pair(ByteArray(4) { arrayOf(0x12, 0x34, 0x56, 0x78)[it].toByte() }, 305419896),
        Pair(ByteArray(4) { arrayOf(0xfe, 0xdc, 0xba, 0x01)[it].toByte() }, -19088895),
    )

    @Test
    fun testBytesToInt() {
        for (c in bytes2IntCases) {
            val n = BytesOps.bytesToInt(c.first, false)
            Assertions.assertEquals(c.second, n)
            val n1 = BytesOps.bytesToInt(c.first.reversedArray(), true)
            Assertions.assertEquals(c.second, n1)
        }
    }

    @Test
    fun testIntToBytes() {
        for (c in bytes2IntCases) {
            val n = BytesOps.intToBytes(c.second, false)
            Assertions.assertTrue(compareByteArray(c.first, n))
            val n1 = BytesOps.intToBytes(c.second, true)
            Assertions.assertTrue(compareByteArray(c.first.reversedArray(), n1))
        }
    }

    private fun compareByteArray(a: ByteArray, b: ByteArray): Boolean {
        logger.debug("compare {} : {}", a, b)
        return when {
            a.size != 4 || a.size != b.size -> false
            IntRange(0, 3).any { a[it] != b[it] } -> false
            else -> true
        }
    }
}
