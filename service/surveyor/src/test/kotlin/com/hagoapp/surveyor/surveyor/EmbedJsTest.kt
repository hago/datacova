/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.surveyor

import com.google.gson.Gson
import com.hagoapp.surveyor.SurveyorFactory
import com.hagoapp.surveyor.rule.EmbedJsRuleConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets

class EmbedJsTest {

    data class EmbedJsTestCase(
        val params: List<Any?>,
        val expect: Boolean
    )

    data class EmbedJsTestConfig(
        val config: EmbedJsRuleConfig,
        val cases: List<EmbedJsTestCase>
    )

    private val tests = listOf(
        "tests/sampleconfig/embedjs/arithmetic.json"
    )

    @Test
    fun testFunction() {
        val configs = tests.map {
            FileInputStream(File(it)).use { fis ->
                val content = fis.readAllBytes().toString(StandardCharsets.UTF_8)
                Gson().fromJson(content, EmbedJsTestConfig::class.java)
            }
        }
        configs.forEach { config ->
            SurveyorFactory.createSurveyor(config.config).use {
                config.cases.forEach { case ->
                    val r = it.process(*case.params.toTypedArray())
                    Assertions.assertEquals(case.expect, r)
                }
            }
        }
    }
}
