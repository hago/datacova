/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.surveyor

import com.hagoapp.surveyor.Constants
import com.hagoapp.surveyor.SurveyorFactory
import com.hagoapp.surveyor.rule.NumberBoundary
import com.hagoapp.surveyor.rule.NumberRangeRuleConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

class NumberRangeTest {

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

    private val logger = LoggerFactory.getLogger(this::class.java)

    private fun open(filename: String): NumberRangeRuleConfig {
        FileInputStream(File(baseDirectory, filename)).use {
            val cfg = SurveyorFactory.createRuleConfig(it)
            if (cfg !is NumberRangeRuleConfig) {
                throw IllegalArgumentException("$filename is not NumberRangeRuleConfig")
            }
            return cfg
        }
    }

    private data class Case(
        val value: Double,
        val expect4EmptyConfig: Boolean,
        val expect4LowerConfig: Boolean,
        val expect4UpperConfig: Boolean,
        val expect4BothConfig: Boolean,
    )

    private val cases = listOf(
        Case(
            -4.0,
            expect4EmptyConfig = true,
            expect4LowerConfig = false,
            expect4UpperConfig = true,
            expect4BothConfig = false
        ),
        Case(
            -2.0,
            expect4EmptyConfig = true,
            expect4LowerConfig = false,
            expect4UpperConfig = true,
            expect4BothConfig = true
        ),
        Case(
            99.123,
            expect4EmptyConfig = true,
            expect4LowerConfig = true,
            expect4UpperConfig = true,
            expect4BothConfig = true
        ),
        Case(
            200.0,
            expect4EmptyConfig = true,
            expect4LowerConfig = true,
            expect4UpperConfig = false,
            expect4BothConfig = false
        )
    )

    @Test
    fun testEmptyNumberRange() {
        val cfg = open(Constants.NUMBER_RANGE_SAMPLE_CONFIG_EMPTY)
        SurveyorFactory.createSurveyor(cfg).use { processor ->
            for (case in cases) {
                logger.debug("test $case for empty config")
                Assertions.assertEquals(case.expect4EmptyConfig, processor.process(listOf(case.value)))
            }
        }
    }

    @Test
    fun testLowerNumberRange() {
        val cfg = open(Constants.NUMBER_RANGE_SAMPLE_CONFIG_LOWER)
        SurveyorFactory.createSurveyor(cfg).use { processor ->
            for (case in cases) {
                logger.debug("test $case for lower config")
                Assertions.assertEquals(case.expect4LowerConfig, processor.process(listOf(case.value)))
            }
        }
    }

    @Test
    fun testUpperNumberRange() {
        val cfg = open(Constants.NUMBER_RANGE_SAMPLE_CONFIG_UPPER)
        SurveyorFactory.createSurveyor(cfg).use { processor ->
            for (case in cases) {
                logger.debug("test $case for upper config")
                Assertions.assertEquals(case.expect4UpperConfig, processor.process(listOf(case.value)))
            }
        }
    }

    @Test
    fun testBothNumberRange() {
        val cfg = open(Constants.NUMBER_RANGE_SAMPLE_CONFIG_BOTH)
        SurveyorFactory.createSurveyor(cfg).use { processor ->
            for (case in cases) {
                logger.debug("test $case for both config")
                Assertions.assertEquals(case.expect4BothConfig, processor.process(listOf(case.value)))
            }
        }
    }

    private val numberRangeCases = listOf(
        Triple(Constants.NUMBER_RANGE_SAMPLE_CONFIG_EMPTY, null, null),
        Triple(Constants.NUMBER_RANGE_SAMPLE_CONFIG_LOWER, NumberBoundary(-2.0, false), null),
        Triple(Constants.NUMBER_RANGE_SAMPLE_CONFIG_UPPER, null, NumberBoundary(99.123, true)),
        Triple(
            Constants.NUMBER_RANGE_SAMPLE_CONFIG_BOTH,
            NumberBoundary(-2.0, true),
            NumberBoundary(99.123, true)
        )
    )

    @Test
    fun testNumberRangeConfig() {
        for (case in numberRangeCases) {
            logger.debug("test $case")
            val f = File(baseDirectory, case.first)
            FileInputStream(f).use {
                val cfg = SurveyorFactory.createRuleConfig(it)
                Assertions.assertTrue(cfg is NumberRangeRuleConfig)
                val nc = cfg as NumberRangeRuleConfig
                Assertions.assertEquals(case.second, nc.lowerBoundary)
                Assertions.assertEquals(case.third, nc.upperBoundary)
            }
        }
    }

}
