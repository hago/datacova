/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.surveyor

import com.hagoapp.surveyor.rule.RegexRuleConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class RegexRuleTest {

    private val logger = LoggerFactory.getLogger(this::class.java)

    data class Case(
        val regex: String,
        val caseSensitive: Boolean,
        val text: String,
        val expect: Boolean
    ) {
        override fun toString(): String {
            return "Case(regex='$regex', caseSensitive=$caseSensitive, text='$text', expect=$expect)"
        }
    }

    private val regexCases = listOf(
        Case("\\d+", false, "123", true),
        Case("\\d+", false, "abc", false),
        Case("\\w+", false, " 123", false),
        Case("\\w+", false, "abc123", true),
        Case("abc", false, "ABC", true),
        Case("abc", true, "ABC", false)
    )

    @Test
    fun testRegex() {
        val config = RegexRuleConfig()
        for (case in regexCases) {
            config.pattern = case.regex
            config.isCaseSensitive = case.caseSensitive
            RegexRuleSurveyor().acceptConfiguration(config).use { processor ->
                val ret = processor.process(listOf(case.text))
                logger.info("test case $case, ${if (ret) "success" else "fail"}")
                Assertions.assertEquals(case.expect, ret)
            }
        }
    }
}
