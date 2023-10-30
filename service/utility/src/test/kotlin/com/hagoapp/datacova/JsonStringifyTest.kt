/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JsonStringifyTest {

    companion object {
        class JsonStringifySample(val id:Int, val name: String): JsonStringify
    }

    @Test
    fun testJsonStringify() {
        val obj = JsonStringifySample(1, "json")
        val json = obj.toJson()
        Assertions.assertNotNull(json)
        Assertions.assertTrue(json.indexOf("${obj.id}") > 0)
        Assertions.assertTrue(json.indexOf("\"${obj.name}\"") > 0)
    }
}
