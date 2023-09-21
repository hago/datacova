/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.datetime;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.f2t.util.DateTimeTypeUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

public class DateUtils {

    private DateUtils() {
    }

    @NotNull
    public static ZonedDateTime beginOfZonedDay(@NotNull ZonedDateTime someTime) {
        return ZonedDateTime.of(someTime.getYear(), someTime.getMonthValue(), someTime.getDayOfMonth(),
                0, 0, 0, 0, someTime.getZone());
    }

    @NotNull
    public static ZonedDateTime endOfZonedDay(@NotNull ZonedDateTime someTime) {
        return someTime.plusDays(1).minusNanos(1);
    }

    @NotNull
    public static LocalDateTime beginOfLocalDay(@NotNull LocalDateTime someTime) {
        return LocalDateTime.of(someTime.getYear(), someTime.getMonthValue(), someTime.getDayOfMonth(),
                0, 0, 0, 0);
    }

    @NotNull
    public static ZonedDateTime beginOfZonedWeek(@NotNull ZonedDateTime someTime) {
        var need = ZonedDateTime.of(someTime.getYear(), someTime.getMonthValue(), someTime.getDayOfMonth(),
                0, 0, 0, 0, someTime.getZone());
        need = need.minusDays(someTime.getDayOfWeek().getValue() - 1L);
        return need;
    }

    @NotNull
    public static ZonedDateTime endOfZonedWeek(@NotNull ZonedDateTime someTime) {
        int weekDay1 = someTime.getDayOfWeek().getValue();
        return someTime.plusDays(7L - weekDay1).minusNanos(1);
    }

    @NotNull
    public static LocalDateTime beginOfLocalWeek(LocalDateTime someTime) {
        var need = LocalDateTime.of(someTime.getYear(), someTime.getMonthValue(), someTime.getDayOfMonth(),
                0, 0, 0, 0);
        need = need.minusDays(someTime.getDayOfWeek().getValue() - 1L);
        return need;
    }

    @NotNull
    public static ZonedDateTime beginOfZonedMonth(ZonedDateTime someTime) {
        return ZonedDateTime.of(someTime.getYear(), someTime.getMonthValue(),
                1, 0, 0, 0, 0, someTime.getZone());
    }

    @NotNull
    public static ZonedDateTime endOfZonedMonth(ZonedDateTime someTime) {
        var dayAfterAMonth = someTime.plusMonths(1);
        return DateUtils.trimZonedDate(dayAfterAMonth, dayAfterAMonth.getMonthValue()).minusNanos(1);
    }

    @NotNull
    public static LocalDateTime beginOfLocalMonth(LocalDateTime someTime) {
        return LocalDateTime.of(someTime.getYear(), someTime.getMonthValue(),
                1, 0, 0, 0, 0);
    }

    @NotNull
    public static ZonedDateTime beginOfZonedQuarter(ZonedDateTime someTime) {
        var need = ZonedDateTime.of(someTime.getYear(), someTime.getMonthValue(),
                1, 0, 0, 0, 0, someTime.getZone());
        need = need.minusMonths((someTime.getMonthValue() - 1) % 3);
        return need;
    }

    @NotNull
    public static ZonedDateTime endOfZonedQuarter(ZonedDateTime someTime) {
        var begin = beginOfZonedQuarter(someTime);
        return begin.plusMonths(3).minusNanos(1);
    }

    @NotNull
    public static LocalDateTime beginOfLocalQuarter(LocalDateTime someTime) {
        var need = LocalDateTime.of(someTime.getYear(), someTime.getMonthValue(),
                1, 0, 0, 0, 0);
        need = need.minusMonths((someTime.getMonthValue() - 1) % 3);
        return need;
    }

    @NotNull
    public static ZonedDateTime beginOfZonedYear(ZonedDateTime someTime) {
        return ZonedDateTime.of(someTime.getYear(), 1, 1,
                0, 0, 0, 0, someTime.getZone());
    }

    @NotNull
    public static ZonedDateTime endOfZonedYear(ZonedDateTime someTime) {
        return beginOfZonedYear(someTime).plusYears(1).minusNanos(1);
    }

    @NotNull
    public static LocalDateTime beginOfLocalYear(LocalDateTime someTime) {
        return LocalDateTime.of(someTime.getYear(), 1, 1,
                0, 0, 0, 0);
    }

    public static long yearMonthOffsetToMilliSecond(@NotNull LocalDateTime base, int monthOffset) {
        return yearMonthOffsetToMilliSecond(base, monthOffset, 0);
    }

    public static long yearMonthOffsetToMilliSecond(@NotNull LocalDateTime base, int monthOffset, int yearOffset) {
        LocalDateTime other = base.plusYears(yearOffset).plusMonths(monthOffset);
        return other.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                base.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static long zonedYearMonthOffsetToMilliSecond(@NotNull ZonedDateTime base, int monthOffset) {
        return zonedYearMonthOffsetToMilliSecond(base, monthOffset, 0);
    }

    public static long zonedYearMonthOffsetToMilliSecond(@NotNull ZonedDateTime base, int monthOffset, int yearOffset) {
        ZonedDateTime other = base.plusYears(yearOffset).plusMonths(monthOffset);
        return other.toInstant().toEpochMilli() - base.toInstant().toEpochMilli();
    }

    public static Long toMillisecondLong(Object src) throws CoVaException {
        if (src == null) {
            return null;
        } else if (src instanceof LocalDateTime) {
            return ((LocalDateTime) src).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } else if (src instanceof ZonedDateTime) {
            return ((ZonedDateTime) src).toInstant().toEpochMilli();
        } else if (src instanceof Long) {
            return (Long) src;
        } else if (src instanceof Integer) {
            return ((Integer) src).longValue();
        } else if (src instanceof Double) {
            return ((Double) src).longValue();
        } else if (src instanceof Float) {
            return ((Float) src).longValue();
        } else if (src instanceof String) {
            try {
                return Objects.requireNonNull(DateTimeTypeUtils.stringToDateTimeOrNull((String) src))
                        .toInstant().toEpochMilli();
            } catch (Exception ex) {
                throw new CoVaException(String.format("not a time compatible value %s", src), ex);
            }
        } else {
            throw new CoVaException(String.format("not a time compatible value %s", src));
        }
    }

    public static Long durationToMilliSecondLong(Object src) {
        if (src == null) {
            return null;
        } else if (src instanceof Integer) {
            return ((Integer) src).longValue();
        } else if (src instanceof Float) {
            return ((Float) src).longValue();
        } else if (src instanceof Double) {
            return ((Double) src).longValue();
        } else if (src instanceof Long) {
            return (Long) src;
        } else if (src instanceof Duration) {
            return ((Duration) src).toMillis();
        } else if (src instanceof String) {
            return Duration.parse((String) src).toMillis();
        } else {
            return null;
        }
    }

    public static class DurationYearMonth {
        private Duration duration = null;
        private Integer year = null;
        private Integer month = null;

        @Override
        public String toString() {
            return String.format("duration: %s  year: %s  month: %s", duration, year, month);
        }
    }

    private static final Pattern durationYearMonthPattern = Pattern.compile("P(-?\\d+Y|)(-?\\d+M|)(-?\\d+D|)(T.*|)");

    public static DurationYearMonth stringToDurationYearMonth(String src) {
        var matcher = durationYearMonthPattern.matcher(src.toUpperCase());
        var dym = new DurationYearMonth();
        if (!matcher.matches()) {
            return dym;
        }
        var y = matcher.group(1);
        var m = matcher.group(2);
        var d = matcher.group(3);
        var t = matcher.group(4);
        dym.duration = t.isEmpty() && d.isEmpty() ? null : Duration.parse(String.format("P%s%s", d, t));
        dym.year = y.isEmpty() ? null : Integer.valueOf(y.substring(0, y.length() - 1));
        dym.month = m.isEmpty() ? null : Integer.valueOf(m.substring(0, m.length() - 1));
        return dym;
    }

    public static ZonedDateTime trimZonedDate(@NotNull ZonedDateTime date) {
        return trimZonedDate(date, 1, 1, 0, 0, 0, 0);
    }

    public static ZonedDateTime trimZonedDate(@NotNull ZonedDateTime date, int month) {
        return trimZonedDate(date, month, 1, 0, 0, 0, 0);
    }

    public static ZonedDateTime trimZonedDate(@NotNull ZonedDateTime date, int month, int dayOfMonth) {
        return trimZonedDate(date, month, dayOfMonth, 0, 0, 0, 0);
    }

    public static ZonedDateTime trimZonedDate(@NotNull ZonedDateTime date, int month, int dayOfMonth, int hour) {
        return trimZonedDate(date, month, dayOfMonth, hour, 0, 0, 0);
    }

    public static ZonedDateTime trimZonedDate(@NotNull ZonedDateTime date, int month, int dayOfMonth,
                                              int hour, int minute) {
        return trimZonedDate(date, month, dayOfMonth, hour, minute, 0, 0);
    }

    public static ZonedDateTime trimZonedDate(@NotNull ZonedDateTime date, int month, int dayOfMonth,
                                              int hour, int minute, int second) {
        return trimZonedDate(date, month, dayOfMonth, hour, minute, second, 0);
    }

    public static ZonedDateTime trimZonedDate(@NotNull ZonedDateTime date, int month, int dayOfMonth,
                                              int hour, int minute, int second, int nanoOfSecond) {
        return ZonedDateTime.of(date.getYear(), month, dayOfMonth, hour, minute, second, nanoOfSecond, date.getZone());
    }
}
