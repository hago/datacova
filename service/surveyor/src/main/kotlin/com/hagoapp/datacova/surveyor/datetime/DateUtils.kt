package com.hagoapp.datacova.surveyor.datetime

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.regex.Pattern

class DateUtils {
    companion object {

        @JvmStatic
        fun beginOfZonedDay(someTime: ZonedDateTime): ZonedDateTime {
            return ZonedDateTime.of(
                someTime.year, someTime.monthValue, someTime.dayOfMonth,
                0, 0, 0, 0, someTime.zone
            )
        }

        @JvmStatic
        fun endOfZonedDay(someTime: ZonedDateTime): ZonedDateTime {
            return someTime.plusDays(1).minusNanos(1)
        }

        @JvmStatic
        fun beginOfLocalDay(someTime: LocalDateTime): LocalDateTime {
            return LocalDateTime.of(
                someTime.year, someTime.monthValue, someTime.dayOfMonth,
                0, 0, 0, 0
            )
        }

        @JvmStatic

        fun beginOfZonedWeek(someTime: ZonedDateTime): ZonedDateTime {
            var need = ZonedDateTime.of(
                someTime.year, someTime.monthValue, someTime.dayOfMonth,
                0, 0, 0, 0, someTime.zone
            )
            need = need.minusDays((someTime.dayOfWeek.value - 1).toLong())
            return need
        }

        @JvmStatic

        fun endOfZonedWeek(someTime: ZonedDateTime): ZonedDateTime {
            val weekDay1 = someTime.dayOfWeek.value
            return someTime.plusDays((7 - weekDay1).toLong()).minusNanos(1)
        }

        @JvmStatic

        fun beginOfLocalWeek(someTime: LocalDateTime): LocalDateTime {
            var need = LocalDateTime.of(
                someTime.year, someTime.monthValue, someTime.dayOfMonth,
                0, 0, 0, 0
            )
            need = need.minusDays((someTime.dayOfWeek.value - 1).toLong())
            return need
        }

        @JvmStatic

        fun beginOfZonedMonth(someTime: ZonedDateTime): ZonedDateTime {
            return ZonedDateTime.of(
                someTime.year, someTime.monthValue,
                1, 0, 0, 0, 0, someTime.zone
            )
        }

        @JvmStatic
        fun endOfZonedMonth(someTime: ZonedDateTime): ZonedDateTime {
            val dayAfterAMonth = someTime.plusMonths(1)
            return trimZonedDate(dayAfterAMonth, dayAfterAMonth.monthValue).minusNanos(1)
        }

        @JvmStatic
        fun beginOfLocalMonth(someTime: LocalDateTime): LocalDateTime {
            return LocalDateTime.of(
                someTime.year, someTime.monthValue,
                1, 0, 0, 0, 0
            )
        }

        @JvmStatic
        fun beginOfZonedQuarter(someTime: ZonedDateTime): ZonedDateTime {
            var need = ZonedDateTime.of(
                someTime.year, someTime.monthValue,
                1, 0, 0, 0, 0, someTime.zone
            )
            need = need.minusMonths(((someTime.monthValue - 1) % 3).toLong())
            return need
        }

        @JvmStatic
        fun endOfZonedQuarter(someTime: ZonedDateTime): ZonedDateTime {
            val begin = beginOfZonedQuarter(someTime)
            return begin.plusMonths(3).minusNanos(1)
        }

        @JvmStatic
        fun beginOfLocalQuarter(someTime: LocalDateTime): LocalDateTime {
            var need = LocalDateTime.of(
                someTime.year, someTime.monthValue,
                1, 0, 0, 0, 0
            )
            need = need.minusMonths(((someTime.monthValue - 1) % 3).toLong())
            return need
        }

        @JvmStatic
        fun beginOfZonedYear(someTime: ZonedDateTime): ZonedDateTime {
            return ZonedDateTime.of(
                someTime.year, 1, 1,
                0, 0, 0, 0, someTime.zone
            )
        }

        @JvmStatic
        fun endOfZonedYear(someTime: ZonedDateTime): ZonedDateTime {
            return beginOfZonedYear(someTime).plusYears(1).minusNanos(1)
        }

        @JvmStatic
        fun beginOfLocalYear(someTime: LocalDateTime): LocalDateTime {
            return LocalDateTime.of(
                someTime.year, 1, 1,
                0, 0, 0, 0
            )
        }

        @JvmStatic
        fun yearMonthOffsetToMilliSecond(base: LocalDateTime, monthOffset: Int): Long {
            return yearMonthOffsetToMilliSecond(base, monthOffset, 0)
        }

        @JvmStatic
        fun yearMonthOffsetToMilliSecond(base: LocalDateTime, monthOffset: Int, yearOffset: Int): Long {
            val other = base.plusYears(yearOffset.toLong()).plusMonths(monthOffset.toLong())
            return other.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                    base.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        @JvmStatic
        fun zonedYearMonthOffsetToMilliSecond(base: ZonedDateTime, monthOffset: Int): Long {
            return zonedYearMonthOffsetToMilliSecond(base, monthOffset, 0)
        }

        @JvmStatic
        fun zonedYearMonthOffsetToMilliSecond(base: ZonedDateTime, monthOffset: Int, yearOffset: Int): Long {
            val other = base.plusYears(yearOffset.toLong()).plusMonths(monthOffset.toLong())
            return other.toInstant().toEpochMilli() - base.toInstant().toEpochMilli()
        }

        @JvmStatic
        fun toMillisecondLong(src: Any?): Long? {
            return when (src) {
                null -> null
                is LocalDateTime -> src.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                is ZonedDateTime -> src.toInstant().toEpochMilli()
                is Long -> src
                is Int -> src.toLong()
                is Double -> src.toLong()
                is Float -> src.toLong()
                is String -> {
                    try {
                        DateTimeTypeUtils.stringToDateTimeOrNull(src)?.toInstant()?.toEpochMilli()
                    } catch (ex: Exception) {
                        throw UnsupportedOperationException(String.format("not a time compatible value %s", src), ex)
                    }
                }

                else -> throw UnsupportedOperationException(String.format("not a time compatible value %s", src))
            }
        }

        @JvmStatic
        fun durationToMilliSecondLong(src: Any?): Long? {
            return when (src) {
                null -> null
                is Int -> src.toLong()
                is Float -> src.toLong()
                is Double -> src.toLong()
                is Long -> src
                is Duration -> src.toMillis()
                is String -> Duration.parse(src as String?).toMillis()
                else -> null
            }
        }

        data class DurationYearMonth(
            var duration: Duration? = null,
            var year: Int? = null,
            var month: Int? = null
        ) {
            override fun toString(): String {
                return String.format("duration: %s  year: %s  month: %s", duration, year, month)
            }
        }

        private val durationYearMonthPattern = Pattern.compile("P(-?\\d+Y|)(-?\\d+M|)(-?\\d+D|)(T.*|)")

        @JvmStatic
        fun stringToDurationYearMonth(src: String): DurationYearMonth {
            val matcher = durationYearMonthPattern.matcher(src.uppercase(Locale.getDefault()))
            val dym = DurationYearMonth()
            if (!matcher.matches()) {
                return dym
            }
            val y = matcher.group(1)
            val m = matcher.group(2)
            val d = matcher.group(3)
            val t = matcher.group(4)
            dym.duration = if (t.isEmpty() && d.isEmpty()) null else Duration.parse(String.format("P%s%s", d, t))
            dym.year = if (y.isEmpty()) null else Integer.valueOf(y.substring(0, y.length - 1))
            dym.month = if (m.isEmpty()) null else Integer.valueOf(m.substring(0, m.length - 1))
            return dym
        }

        @JvmStatic
        fun trimZonedDate(date: ZonedDateTime): ZonedDateTime {
            return trimZonedDate(date, 1, 1, 0, 0, 0, 0)
        }

        @JvmStatic
        fun trimZonedDate(date: ZonedDateTime, month: Int): ZonedDateTime {
            return trimZonedDate(date, month, 1, 0, 0, 0, 0)
        }

        @JvmStatic
        fun trimZonedDate(date: ZonedDateTime, month: Int, dayOfMonth: Int): ZonedDateTime {
            return trimZonedDate(date, month, dayOfMonth, 0, 0, 0, 0)
        }

        @JvmStatic
        fun trimZonedDate(date: ZonedDateTime, month: Int, dayOfMonth: Int, hour: Int): ZonedDateTime {
            return trimZonedDate(date, month, dayOfMonth, hour, 0, 0, 0)
        }

        @JvmStatic
        fun trimZonedDate(
            date: ZonedDateTime, month: Int, dayOfMonth: Int,
            hour: Int, minute: Int
        ): ZonedDateTime {
            return trimZonedDate(date, month, dayOfMonth, hour, minute, 0, 0)
        }

        @JvmStatic
        fun trimZonedDate(
            date: ZonedDateTime, month: Int, dayOfMonth: Int,
            hour: Int, minute: Int, second: Int
        ): ZonedDateTime {
            return trimZonedDate(date, month, dayOfMonth, hour, minute, second, 0)
        }

        @JvmStatic
        fun trimZonedDate(
            date: ZonedDateTime, month: Int, dayOfMonth: Int,
            hour: Int, minute: Int, second: Int, nanoOfSecond: Int
        ): ZonedDateTime {
            return ZonedDateTime.of(date.year, month, dayOfMonth, hour, minute, second, nanoOfSecond, date.zone)
        }
    }
}
