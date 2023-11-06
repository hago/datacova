package com.hagoapp.datacova.surveyor.surveyor

import com.hagoapp.surveyor.Constants.*
import com.hagoapp.datacova.surveyor.SurveyorFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeRangeTest {
    private data class Case(
        val value: ZonedDateTime,
        val expect4EmptyConfig: Boolean,
        val expect4LowerConfig: Boolean,
        val expect4UpperConfig: Boolean,
        val expect4BothConfig: Boolean,
    ) {
        override fun toString(): String {
            return "Case(value=$value, expect4EmptyConfig=$expect4EmptyConfig, expect4LowerConfig=$expect4LowerConfig, expect4UpperConfig=$expect4UpperConfig, expect4BothConfig=$expect4BothConfig)"
        }
    }

    companion object {
        private var baseDirectory: String = "./tests/sampleconfig"

        private val emptySurveyor: TimeRangeRuleSurveyor
        private val lowerSurveyor: TimeRangeRuleSurveyor
        private val upperSurveyor: TimeRangeRuleSurveyor
        private val bothSurveyor: TimeRangeRuleSurveyor

        init {
            bothSurveyor = readFile(TIME_RANGE_SAMPLE_CONFIG_BOTH)
                .use { SurveyorFactory.createSurveyor(it) } as TimeRangeRuleSurveyor
            lowerSurveyor = readFile(TIME_RANGE_SAMPLE_CONFIG_LOWER)
                .use { SurveyorFactory.createSurveyor(it) } as TimeRangeRuleSurveyor
            upperSurveyor = readFile(TIME_RANGE_SAMPLE_CONFIG_UPPER)
                .use { SurveyorFactory.createSurveyor(it) } as TimeRangeRuleSurveyor
            emptySurveyor = readFile(TIME_RANGE_SAMPLE_CONFIG_NULL)
                .use { SurveyorFactory.createSurveyor(it) } as TimeRangeRuleSurveyor
        }

        private fun readFile(fileName: String): InputStream {
            return FileInputStream(File(baseDirectory, fileName))
        }
    }

    private val cases = listOf(
        Case(
            ZonedDateTime.of(2018, 2, 23, 15, 1, 2, 345, ZoneId.of("Asia/Shanghai")),
            expect4EmptyConfig = true,
            expect4LowerConfig = false,
            expect4UpperConfig = true,
            expect4BothConfig = true
        ),
        Case(
            ZonedDateTime.of(2018, 2, 20, 15, 1, 2, 345, ZoneId.of("Asia/Shanghai")),
            expect4EmptyConfig = true,
            expect4LowerConfig = false,
            expect4UpperConfig = true,
            expect4BothConfig = false
        ),
        Case(
            ZonedDateTime.of(2022, 12, 10, 16, 52, 0, 0, ZoneId.of("Asia/Shanghai")),
            expect4EmptyConfig = true,
            expect4LowerConfig = true,
            expect4UpperConfig = false,
            expect4BothConfig = true
        ),
        Case(
            ZonedDateTime.of(2023, 12, 10, 16, 52, 0, 0, ZoneId.of("Asia/Shanghai")),
            expect4EmptyConfig = true,
            expect4LowerConfig = true,
            expect4UpperConfig = false,
            expect4BothConfig = false
        )
    )

    private val logger = LoggerFactory.getLogger(TimeRangeTest::class.java)

    @Test
    fun testTimeRangeTest() {
        for (case in cases) {
            val l = case.value.toInstant().toEpochMilli()
            logger.debug(
                "test {} {}, expect empty:{} lower:{} upper:{} both:{}",
                l,
                case.value,
                case.expect4EmptyConfig,
                case.expect4LowerConfig,
                case.expect4UpperConfig,
                case.expect4BothConfig
            )
            Assertions.assertEquals(case.expect4BothConfig, bothSurveyor.process(l))
            Assertions.assertEquals(case.expect4LowerConfig, lowerSurveyor.process(l))
            Assertions.assertEquals(case.expect4UpperConfig, upperSurveyor.process(l))
            Assertions.assertEquals(case.expect4EmptyConfig, emptySurveyor.process(l))
        }
    }
}
