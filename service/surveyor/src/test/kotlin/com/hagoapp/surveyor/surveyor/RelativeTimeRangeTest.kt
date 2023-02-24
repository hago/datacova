package com.hagoapp.surveyor.surveyor

import com.hagoapp.surveyor.SurveyorFactory
import com.hagoapp.surveyor.rule.RelativeTimeBoundary
import com.hagoapp.surveyor.rule.RelativeTimeRangeConfig
import com.hagoapp.surveyor.utils.datetime.TimeAnchor
import com.hagoapp.surveyor.utils.datetime.TimeDiff
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class RelativeTimeRangeTest {
    @Test
    fun testYesterday() {
        val nowMilli = Instant.now().toEpochMilli()
        val random = Random(nowMilli)
        val sometimeYesterday = nowMilli - random.nextLong(86400 * 1000)
        val config = RelativeTimeRangeConfig(
            RelativeTimeBoundary(
                TimeAnchor.Now, TimeDiff(
                    0, 0, 1, 0, 0, 0, false
                ), true
            ),
            RelativeTimeBoundary(
                TimeAnchor.Now, TimeDiff(
                    0, 0, 0, 0, 0, 0, false
                ), true
            )
        )
        SurveyorFactory.createSurveyor(config).use { surveyor ->
            val result = surveyor.process(sometimeYesterday)
            Assertions.assertTrue(result)
        }
    }

    @Test
    fun testTomorrow() {
        val nowMilli = Instant.now().toEpochMilli()
        val random = Random(nowMilli)
        val sometime = nowMilli + random.nextLong(86400 * 1000)
        val config = RelativeTimeRangeConfig(
            RelativeTimeBoundary(
                TimeAnchor.Now, TimeDiff(
                    0, 0, 0, 0, 0, 0, false
                ), true
            ),
            RelativeTimeBoundary(
                TimeAnchor.Now, TimeDiff(
                    0, 0, 1, 0, 0, 0, true
                ), true
            )
        )
        SurveyorFactory.createSurveyor(config).use { surveyor ->
            val result = surveyor.process(sometime)
            Assertions.assertTrue(result)
        }
    }

    @Test
    fun testLastWeek() {
        val d0 = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val d = d0.minusDays(d0.dayOfWeek.ordinal.toLong()).minusNanos(1)
        val nowMilli = d.toInstant().toEpochMilli()
        val random = Random(nowMilli)
        val sometime = nowMilli - random.nextLong(86400 * 7 * 1000)
        val config = RelativeTimeRangeConfig(
            RelativeTimeBoundary(
                TimeAnchor.BeginOfThisWeek, TimeDiff(
                    0, 0, 7, 0, 0, 0, false
                ), true
            ),
            RelativeTimeBoundary(
                TimeAnchor.BeginOfThisWeek, TimeDiff.ZeroDiff, true
            )
        )
        SurveyorFactory.createSurveyor(config).use { surveyor ->
            val result = surveyor.process(sometime)
            Assertions.assertTrue(result)
        }
    }
}
