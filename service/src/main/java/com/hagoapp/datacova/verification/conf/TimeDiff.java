/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.verification.conf;

import java.time.ZonedDateTime;

public class TimeDiff {

    private int year = 0;
    private int month = 0;
    private int day = 0;
    private int hour = 0;
    private int direction = 1;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction <= 0 ? -1 : 1;
    }

    public ZonedDateTime apply(ZonedDateTime aTime) {
        return aTime.plusYears((long) year * direction).plusMonths((long) month * direction)
                .plusDays((long) day * direction).plusHours((long) hour * direction);
    }

    public static TimeDiff Zero = new TimeDiff();
}
