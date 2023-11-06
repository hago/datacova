/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.surveyor.surveyor

import com.hagoapp.surveyor.Constants
import com.hagoapp.datacova.surveyor.SurveyorFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

class OptionsRuleTest {

    companion object {
        private var baseDirectory: String = "./tests/sampleconfig"
        private val logger = LoggerFactory.getLogger(this::class.java)

        @JvmStatic
        @BeforeAll
        fun load(): Unit {
            val props = System.getProperties()
            if (props.containsKey(Constants.CONFIG_TEST_BASE_DIRECTORY)) {
                baseDirectory = props[Constants.CONFIG_TEST_BASE_DIRECTORY].toString()
            }
        }
    }

    private data class Case(
        val value: String?,
        val expect4Basic: Boolean,
        val expect4Nullable: Boolean,
        val expect4NullOption: Boolean,
        val expect4CaseInsensitive: Boolean
    )

    private val cases = listOf(
        Case(
            null,
            expect4Basic = false,
            expect4Nullable = false,
            expect4NullOption = true,
            expect4CaseInsensitive = false
        ),
        Case(
            "ABC",
            expect4Basic = false,
            expect4Nullable = false,
            expect4NullOption = false,
            expect4CaseInsensitive = true
        ),
        Case(
            "abc",
            expect4Basic = true,
            expect4Nullable = true,
            expect4NullOption = true,
            expect4CaseInsensitive = true
        )
    )

    private fun createSurveyor(filename: String): Surveyor {
        return FileInputStream(File(baseDirectory, filename)).use {
            SurveyorFactory.createSurveyor(it)
        }
    }

    @Test
    fun testOptionsBasic() {
        createSurveyor(Constants.OPTIONS_SAMPLE_BASIC).use { surveyor ->
            for (case in cases) {
                val r = surveyor.process(listOf(case.value))
                Assertions.assertEquals(case.expect4Basic, r)
            }
        }
    }

    @Test
    fun testOptionsNullable() {
        createSurveyor(Constants.OPTIONS_SAMPLE_NULLABLE).use { surveyor ->
            for (case in cases) {
                val r = surveyor.process(listOf(case.value))
                Assertions.assertEquals(case.expect4Nullable, r)
            }
        }
    }

    @Test
    fun testOptionsNullOption() {
        createSurveyor(Constants.OPTIONS_SAMPLE_NULL_OPTION).use { surveyor ->
            for (case in cases) {
                logger.debug("test $case")
                val r = surveyor.process(listOf(case.value))
                Assertions.assertEquals(case.expect4NullOption, r)
            }
        }
    }

    @Test
    fun testOptionsCaseInsensitive() {
        createSurveyor(Constants.OPTIONS_SAMPLE_CASE_INSENSITIVE).use { surveyor ->
            for (case in cases) {
                val r = surveyor.process(listOf(case.value))
                Assertions.assertEquals(case.expect4CaseInsensitive, r)
            }
        }
    }
}
