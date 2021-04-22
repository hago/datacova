/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.action.verification.conf;

import com.hagoapp.datacova.entity.action.verification.Configuration;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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

    private static final int TIME_RANGE_CONFIGURATION_TYPE = 4;
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
}
