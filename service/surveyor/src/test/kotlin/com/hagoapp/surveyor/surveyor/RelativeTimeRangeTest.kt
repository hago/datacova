package com.hagoapp.surveyor.surveyor

import com.hagoapp.surveyor.SurveyorFactory
import com.hagoapp.surveyor.rule.RelativeTimeBoundary
import com.hagoapp.surveyor.rule.RelativeTimeRangeConfig
import com.hagoapp.surveyor.utils.datetime.TimeAnchor
import com.hagoapp.surveyor.utils.datetime.TimeDiff
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.random.Random

class RelativeTimeRangeTest {
    @Test
    fun testYesterday() {
        val nowMilli = Instant.now().toEpochMilli()
        val random = Random(nowMilli)
        val sometimeYesterday = nowMilli - random.nextInt(86400)
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
        val sometimeYesterday = nowMilli + random.nextInt(86400)
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
            val result = surveyor.process(sometimeYesterday)
            Assertions.assertTrue(result)
        }
    }

    @Test
    fun testWithinOneWeekAgoAndOneWeekLater() {
        val nowMilli = Instant.now().toEpochMilli()
        val random = Random(nowMilli)
        val sometimeYesterday = nowMilli + random.nextInt(86400 * 7) * (if (random.nextBoolean()) 1 else -1)
        val config = RelativeTimeRangeConfig(
            RelativeTimeBoundary(
                TimeAnchor.BeginOfThisWeek, TimeDiff(
                    0, 0, random.nextInt(-7, 0), 0, 0, 0, false
                ), true
            ),
            RelativeTimeBoundary(
                TimeAnchor.BeginOfThisWeek, TimeDiff(
                    0, 0, random.nextInt(7), 0, 0, 0, true
                ), true
            )
        )
        SurveyorFactory.createSurveyor(config).use { surveyor ->
            val result = surveyor.process(sometimeYesterday)
            Assertions.assertTrue(result)
        }
    }
}
