/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility

import com.hagoapp.datacova.utility.text.TextResources
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class TextResourceTest {
    @Test
    fun textLoad() {
        val cases = listOf(
            Case(
                name = "demo",
                locale = Locale.SIMPLIFIED_CHINESE,
                text = "这是一个中国(简体中文)的文本例子"
            ),
            Case(
                name = "demo",
                locale = Locale.UK,
                text = "This is a text demo as default"
            ),
            Case(
                name = "demo/subdemo",
                locale = Locale.US,
                text = "This is a text demo for en_US."
            ),
            Case(
                name = "demo/subdemo",
                locale = Locale.UK,
                text = null
            )
        )
        cases.forEach { case ->
            val s = TextResources.getString(case.locale, case.name)
            Assertions.assertEquals(case.text, s)
        }
        Assertions.assertThrows(CoVaException::class.java) {
            TextResources.getString(Locale.UK, "demo//subdemo")
        }
    }

    private data class Case(
        val name: String,
        val locale: Locale,
        val text: String?
    )
}
