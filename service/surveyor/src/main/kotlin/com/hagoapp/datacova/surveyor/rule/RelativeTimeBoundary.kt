package com.hagoapp.datacova.surveyor.rule

import com.hagoapp.datacova.surveyor.datetime.DateUtils
import com.hagoapp.datacova.surveyor.datetime.TimeAnchor
import com.hagoapp.datacova.surveyor.datetime.TimeDiff
import java.time.Instant
import java.time.ZonedDateTime
import java.util.*

data class RelativeTimeBoundary(
    val anchor: TimeAnchor,
    val diff: TimeDiff,
    val inclusive: Boolean = true,
    val timeZoneName: String = "UTC"
) {
    fun calculateTime(offset: Long = 0L): ZonedDateTime {
        val it = Instant.ofEpochMilli(Instant.now().toEpochMilli() + offset)
        val aTime = ZonedDateTime.ofInstant(it, TimeZone.getTimeZone(timeZoneName).toZoneId())
        return when (anchor) {
            TimeAnchor.Now -> aTime
            TimeAnchor.BeginOfToday -> DateUtils.beginOfZonedDay(aTime)
            TimeAnchor.EndOfToday -> DateUtils.endOfZonedDay(aTime)
            TimeAnchor.BeginOfThisWeek -> DateUtils.beginOfZonedWeek(aTime)
            TimeAnchor.EndOfThisWeek -> DateUtils.endOfZonedWeek(aTime)
            TimeAnchor.BeginOfThisMonth -> DateUtils.beginOfZonedMonth(aTime)
            TimeAnchor.EndOfThisMonth -> DateUtils.endOfZonedMonth(aTime).minusNanos(1)
            TimeAnchor.BeginOfThisQuarter -> DateUtils.beginOfZonedQuarter(aTime)
            TimeAnchor.EndOfThisQuarter -> DateUtils.endOfZonedQuarter(aTime)
            TimeAnchor.BeginOfThisYear -> DateUtils.beginOfZonedYear(aTime)
            TimeAnchor.EndOfThisYear -> DateUtils.endOfZonedYear(aTime)
        }
    }

    fun calculateEpochMilli(): Long {
        return diff.apply(calculateTime()).toInstant().toEpochMilli()
    }
}
