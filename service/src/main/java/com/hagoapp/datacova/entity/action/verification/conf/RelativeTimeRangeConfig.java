/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.action.verification.conf;

import com.hagoapp.datacova.entity.action.verification.Configuration;
import com.hagoapp.datacova.util.datetime.DateUtils;
import com.hagoapp.datacova.util.datetime.TimeReference;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.TimeZone;

public class RelativeTimeRangeConfig extends Configuration {

    private static final int RELATIVE_TIME_RANGE_CONFIGURATION_TYPE = 6;

    public static class RelativeBoundary extends TimeRangeConfig.Boundary {
        private TimeReference reference = TimeReference.Now;
        private TimeDiff timeDiff = TimeDiff.Zero;
        private String timeZoneName = "UTC";

        public TimeReference getReference() {
            return reference;
        }

        public void setReference(TimeReference reference) {
            this.reference = reference;
        }

        public TimeDiff getTimeDiff() {
            return timeDiff;
        }

        public void setTimeDiff(TimeDiff timeDiff) {
            this.timeDiff = timeDiff;
        }

        public String getTimeZoneName() {
            return timeZoneName;
        }

        public void setTimeZoneName(String timeZoneName) {
            this.timeZoneName = timeZoneName;
        }

        private ZonedDateTime calculateTimeReference(TimeReference ref) {
            return calculateTimeReference(ref, 0);
        }

        private ZonedDateTime calculateTimeReference(TimeReference ref, long offset) {
            Instant it = Instant.ofEpochMilli(Instant.now().toEpochMilli() + offset);
            ZonedDateTime aTime = ZonedDateTime.ofInstant(it, TimeZone.getTimeZone(timeZoneName).toZoneId());
            switch (ref) {
                case Now:
                    return aTime;
                case BeginOfToday:
                    return DateUtils.beginOfZonedDay(aTime);
                case EndOfToday:
                    return DateUtils.endOfZonedDay(aTime);
                case BeginOfThisWeek:
                    return DateUtils.beginOfZonedWeek(aTime);
                case EndOfThisWeek:
                    return DateUtils.endOfZonedWeek(aTime);
                case BeginOfThisMonth:
                    return DateUtils.beginOfZonedMonth(aTime);
                case EndOfThisMonth:
                    return DateUtils.endOfZonedMonth(aTime).minusNanos(1);
                case BeginOfThisQuarter:
                    return DateUtils.beginOfZonedQuarter(aTime);
                case EndOfThisQuarter:
                    return DateUtils.endOfZonedQuarter(aTime);
                case BeginOfThisYear:
                    return DateUtils.beginOfZonedYear(aTime);
                case EndOfThisYear:
                    return DateUtils.endOfZonedYear(aTime);
                case BeginOfThisFinancialYear:
                    return DateUtils.beginOfZonedFinancialYear(aTime);
                case EndOfThisFinancialYear:
                    return DateUtils.endOfZonedFinancialYear(aTime);
                default:
                    return null;
            }
        }

        public void calculateValue() {
            setValue(timeDiff.apply(calculateTimeReference(reference)).toInstant().toEpochMilli());
        }
    }

    public RelativeTimeRangeConfig() {
        super();
        type = RELATIVE_TIME_RANGE_CONFIGURATION_TYPE;
    }

    private RelativeBoundary lowerBound;
    private RelativeBoundary upperBound;

    public RelativeBoundary getLowerBound() {
        if (lowerBound == null) {
            return null;
        } else {
            lowerBound.calculateValue();
            return lowerBound;
        }
    }

    public RelativeBoundary getUpperBound() {
        if (upperBound == null) {
            return null;
        } else {
            upperBound.calculateValue();
            return upperBound;
        }
    }

    public void setLowerBound(RelativeBoundary lowerBound) {
        this.lowerBound = lowerBound;
    }

    public void setUpperBound(RelativeBoundary upperBound) {
        this.upperBound = upperBound;
    }
}
