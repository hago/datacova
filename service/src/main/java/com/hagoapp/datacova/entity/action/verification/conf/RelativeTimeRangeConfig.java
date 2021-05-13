/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.action.verification.conf;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.entity.action.verification.Configuration;
import com.hagoapp.datacova.util.datetime.DateUtils;
import com.hagoapp.datacova.util.datetime.TimeReference;
import com.hagoapp.datacova.util.text.TextResourceManager;
import org.stringtemplate.v4.ST;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class RelativeTimeRangeConfig extends Configuration {

    public static final int RELATIVE_TIME_RANGE_CONFIGURATION_TYPE = 6;

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

    @Override
    protected String createDescription(Locale locale) throws CoVaException {
        String format = TextResourceManager.getManager().getString(locale, "/validators/relative_time_range");
        if (format == null) {
            throw new CoVaException("Description for TimeRangeConfig class not found");
        }
        List<String> fields = getFields();
        if (fields.size() == 0) {
            throw new CoVaException("No fields defined in TimeRangeConfig class");
        }
        ST st = new ST(format);
        st.add("fields", fields);
        st.add("lowerBound", lowerBound);
        st.add("upperBound", upperBound);
        if (lowerBound != null) {
            lowerBound.calculateValue();
            st.add(String.format("isLower%s", lowerBound.reference.name()), true);
            st.add("isLowerBefore", lowerBound.timeDiff.getDirection() == -1);
        }
        if (upperBound != null) {
            upperBound.calculateValue();
            st.add(String.format("isUpper%s", upperBound.reference.name()), true);
            st.add("isUpperBefore", upperBound.timeDiff.getDirection() == -1);
        }
        st.add("lowerTime", getEpochMilliString(locale, lowerBound == null ? null : lowerBound.getValue()));
        st.add("upperTime", getEpochMilliString(locale, upperBound == null ? null : upperBound.getValue()));
        createTimeDiffFlags("Lower", lowerBound).forEach(st::add);
        createTimeDiffFlags("Upper", upperBound).forEach(st::add);
        return st.render();
    }

    private Map<String, Object> createTimeDiffFlags(String prefix, RelativeBoundary boundary) {
        return (boundary == null) ? Map.of() :
                Map.of(
                        String.format("is%sYear", prefix), boundary.timeDiff.getYear() > 0,
                        String.format("is%sYearPlural", prefix), boundary.timeDiff.getYear() > 1,
                        String.format("is%sMonth", prefix), boundary.timeDiff.getMonth() > 0,
                        String.format("is%sMonthPlural", prefix), boundary.timeDiff.getMonth() > 1,
                        String.format("is%sDay", prefix), boundary.timeDiff.getDay() > 0,
                        String.format("is%sDayPlural", prefix), boundary.timeDiff.getDay() > 1,
                        String.format("is%sHour", prefix), boundary.timeDiff.getHour() > 0,
                        String.format("is%sHourPlural", prefix), boundary.timeDiff.getHour() > 1
                );
    }

    private String getEpochMilliString(Locale locale, Long milli) {
        if (milli == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneId.systemDefault()).toString();
    }
}
