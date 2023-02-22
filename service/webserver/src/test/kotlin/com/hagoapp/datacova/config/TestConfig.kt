/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config

import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

data class TestConfig(
    val mail: TestMailConfig
) {
    companion object {
        fun load(filename: String): TestConfig {
            FileInputStream(filename).use { fi ->
                ByteArrayOutputStream().use { baos ->
                    val buffer = ByteArray(1024)
                    while (true) {
                        val i = fi.read(buffer, 0, 1024)
                        if (i < 0) {
                            break
                        } else {
                            baos.write(buffer, 0, i)
                        }
                    }
                    val text = String(baos.toByteArray(), StandardCharsets.UTF_8)
                    return Gson().fromJson(text, TestConfig::class.java)
                }
            }
        }
    }
}
