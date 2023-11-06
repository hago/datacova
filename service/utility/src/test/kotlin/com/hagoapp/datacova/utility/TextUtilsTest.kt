/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility

import com.hagoapp.datacova.utility.text.TextUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TextUtilsTest {
    @Test
    fun testIsHtml() {
        mapOf(
            "This is a sentence" to false,
            "<a>This is a sentence" to true,
            "'<a>This is a sentence'" to false,
            "\"<a>This is a sentence\"" to false,
            "\"<a>This is a 'sentence'\"" to false,
            "'<a>This is a \"sentence\"'" to false,
            "<a>This is a \"sentence\"" to true,
            "<a>This is a 'sentence'" to true,
            "<a>'This is a \"sentence\"'" to true,
            "<a>\"This is a 'sentence'\"" to true
        ).forEach { (text, result) ->
            println("evaluate $text, $result expected")
            Assertions.assertEquals(TextUtils.isHtml(text), result)
        }
    }
}
