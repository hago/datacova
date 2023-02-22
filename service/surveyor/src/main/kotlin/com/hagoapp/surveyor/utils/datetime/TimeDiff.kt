package com.hagoapp.surveyor.utils.datetime

import java.time.ZonedDateTime

data class TimeDiff(
    var year: Int,
    var month: Int,
    var day: Int,
    var hour: Int,
    var minute: Int = 0,
    var second: Int = 0,
    var isLaterThan: Boolean = true
) {
    fun apply(t: ZonedDateTime): ZonedDateTime {
        val direction = if (isLaterThan) 1L else -1L
        return t.plusYears(year * direction)
            .plusMonths(month * direction)
            .plusDays(day * direction)
            .plusHours(hour * direction)
            .plusMinutes(minute * direction)
            .plusSeconds(second * direction)
    }
}
