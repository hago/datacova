/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor

import com.hagoapp.datacova.surveyor.SurveyorFactory
import com.hagoapp.datacova.surveyor.rule.*
import com.hagoapp.datacova.surveyor.surveyor.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileInputStream

class SurveyorFactoryTest {

    companion object {
        private var baseDirectory: String = "./tests/sampleconfig"

        @JvmStatic
        @BeforeAll
        fun init(): Unit {
            val props = System.getProperties()
            if (props.containsKey(Constants.CONFIG_TEST_BASE_DIRECTORY)) {
                baseDirectory = props[Constants.CONFIG_TEST_BASE_DIRECTORY].toString()
            }
        }
    }

    private val cases = listOf(
        Triple("regexrule.json", RegexRuleConfig::class.java, RegexRuleSurveyor::class.java),
        Triple(
            Constants.NUMBER_RANGE_SAMPLE_CONFIG_EMPTY, NumberRangeRuleConfig::class.java,
            NumberRangeRuleSurveyor::class.java
        ),
        Triple(
            Constants.NUMBER_RANGE_SAMPLE_CONFIG_LOWER, NumberRangeRuleConfig::class.java,
            NumberRangeRuleSurveyor::class.java
        ),
        Triple(
            Constants.NUMBER_RANGE_SAMPLE_CONFIG_UPPER, NumberRangeRuleConfig::class.java,
            NumberRangeRuleSurveyor::class.java
        ),
        Triple(
            Constants.NUMBER_RANGE_SAMPLE_CONFIG_BOTH, NumberRangeRuleConfig::class.java,
            NumberRangeRuleSurveyor::class.java
        ),
        Triple(
            Constants.OPTIONS_SAMPLE_BASIC, OptionsRuleConfig::class.java,
            OptionsRuleSurveyor::class.java
        ),
        Triple(
            Constants.OPTIONS_SAMPLE_NULLABLE, OptionsRuleConfig::class.java,
            OptionsRuleSurveyor::class.java
        ),
        Triple(
            Constants.OPTIONS_SAMPLE_NULL_OPTION, OptionsRuleConfig::class.java,
            OptionsRuleSurveyor::class.java
        ),
        Triple(
            Constants.OPTIONS_SAMPLE_CASE_INSENSITIVE, OptionsRuleConfig::class.java,
            OptionsRuleSurveyor::class.java
        ),
        Triple(
            Constants.EMBED_JYTHON_SAMPLE_BASIC, EmbedJythonRuleConfig::class.java,
            EmbedJythonSurveyor::class.java
        ),
        Triple(
            Constants.TIME_RANGE_SAMPLE_CONFIG_BOTH, TimeRangeRuleConfig::class.java,
            TimeRangeRuleSurveyor::class.java
        )
    )

    @Test
    fun testLoadSurveyor() {
        for (case in cases) {
            val f = File(baseDirectory, case.first)
            FileInputStream(f).use {
                val cfg = SurveyorFactory.createRuleConfig(it)
                Assertions.assertEquals(case.second, cfg::class.java)
                SurveyorFactory.createSurveyor(cfg).use { processor ->
                    Assertions.assertEquals(case.third, processor::class.java)
                }
            }
        }
    }
}
