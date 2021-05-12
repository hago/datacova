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
import com.hagoapp.datacova.util.text.TextResourceManager;
import org.stringtemplate.v4.ST;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

public class TimeRangeConfig extends Configuration {
    public static class Boundary {
        private long value;
        private boolean inclusive = true;

        public long getValue() {
            return value;
        }

        public void setValue(long value) {
            this.value = value;
        }

        public boolean isInclusive() {
            return inclusive;
        }

        public void setInclusive(boolean inclusive) {
            this.inclusive = inclusive;
        }
    }

    public static final int TIME_RANGE_CONFIGURATION_TYPE = 4;
    private Boundary upperBound;
    private Boundary lowerBound;

    public TimeRangeConfig() {
        super();
        type = TIME_RANGE_CONFIGURATION_TYPE;
    }

    public void setUpperInclusive(boolean value) {
        if (this.upperBound == null) {
            this.upperBound = new Boundary();
        }
        this.upperBound.inclusive = value;
    }

    public void setLowerInclusive(boolean value) {
        if (this.lowerBound == null) {
            this.lowerBound = new Boundary();
        }
        this.lowerBound.inclusive = value;
    }

    public void setUpperTime(long time) {
        if (this.upperBound == null) {
            this.upperBound = new Boundary();
        }
        this.upperBound.value = time;
    }

    public void setUpperTime(Timestamp value) {
        this.setUpperTime(value.toInstant().toEpochMilli());
    }

    public void setUpperTime(LocalDateTime value) {
        this.setUpperTime(ZonedDateTime.of(value, ZoneId.systemDefault()));
    }

    public void setUpperTime(ZonedDateTime value) {
        this.setUpperTime(value.toInstant().toEpochMilli());
    }

    public void setLowerTime(long time) {
        if (this.upperBound == null) {
            this.upperBound = new Boundary();
        }
        this.upperBound.value = time;
    }

    public void settLowerTime(Timestamp value) {
        this.setUpperTime(value.toInstant().toEpochMilli());
    }

    public void settLowerTime(LocalDateTime value) {
        this.setUpperTime(ZonedDateTime.of(value, ZoneId.systemDefault()));
    }

    public void settLowerTime(ZonedDateTime value) {
        this.setUpperTime(value.toInstant().toEpochMilli());
    }

    public Boundary getLowerBound() {
        return lowerBound;
    }

    public Boundary getUpperBound() {
        return upperBound;
    }

    @Override
    public boolean isValid() {
        if ((lowerBound == null) && (upperBound == null)) {
            return false;
        } else if ((lowerBound != null) && (upperBound != null)) {
            if (lowerBound.value > upperBound.value) {
                return false;
            }
        }
        return super.isValid();
    }

    @Override
    protected String createDescription(Locale locale) throws CoVaException {
        String format = TextResourceManager.getManager().getString(locale, "/validators/time_range");
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
        st.add("lowerTime", getEpochMilliString(locale, lowerBound == null ? null : lowerBound.value));
        st.add("upperTime", getEpochMilliString(locale, upperBound == null ? null : upperBound.value));
        return st.render();
    }

    private String getEpochMilliString(Locale locale, Long milli) {
        if (milli == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(milli), ZoneId.systemDefault()).toString();
    }
}
