/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.idle;

import com.hagoapp.datacova.JsonStringify;

public class Configuration implements JsonStringify {
    private int stepCount;
    private long minMilliSeconds;
    private long maxMilliSeconds;

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    public long getMinMilliSeconds() {
        return minMilliSeconds;
    }

    public void setMinMilliSeconds(long minMilliSeconds) {
        this.minMilliSeconds = minMilliSeconds;
    }

    public long getMaxMilliSeconds() {
        return maxMilliSeconds;
    }

    public void setMaxMilliSeconds(long maxMilliSeconds) {
        this.maxMilliSeconds = maxMilliSeconds;
    }
}
