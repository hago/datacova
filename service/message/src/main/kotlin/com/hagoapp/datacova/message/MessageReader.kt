/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.google.gson.GsonBuilder
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.nio.charset.StandardCharsets

/**
 * Message reader to read message object into byte buffer.
 *
 * @author suncjs
 * @since 0.5
 * @constructor Create empty Message reader
 */
class MessageReader : Closeable {

    companion object {
        private val gson = GsonBuilder().create()

        @JvmStatic
        fun readMessage(data: ByteArray): Any? {
            return MessageReader().use {
                it.update(data)
                it.parseMessage()
            }
        }
    }

    private val buffer = ByteArrayOutputStream()
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun update(data: ByteArray) {
        buffer.writeBytes(data)
    }

    fun parseMessage(): Any? {
        if (buffer.size() == 0) {
            return null
        }
        val buf = buffer.toByteArray()
        val clz = MessageFinder.findMessageType(buf[0])
        if (clz == null) {
            logger.error("Unknown type {}", buf[0])
            return null
        }
        val json = String(buf, 1, buf.size - 1, StandardCharsets.UTF_8)
        return gson.fromJson(json, clz)
    }

    override fun close() {
        buffer.close()
    }
}
