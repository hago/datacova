/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.google.gson.GsonBuilder
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

class MessageWriter {
    companion object {
        private val gson = GsonBuilder().create()

        fun toBytes(obj: Any): ByteArray {
            val anno = obj.javaClass.getAnnotation(WorkerMessage::class.java)
                ?: throw UnsupportedOperationException("Message must be annotated with WorkerMessage")
            ByteArrayOutputStream().use {
                it.write(anno.type.toInt())
                val json = gson.toJson(obj)
                it.writeBytes(json.toByteArray(StandardCharsets.UTF_8))
                return it.toByteArray()
            }
        }
    }
}
