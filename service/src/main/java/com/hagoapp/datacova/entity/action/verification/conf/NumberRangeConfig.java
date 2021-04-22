/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.verification.conf;

import com.hagoapp.datacova.entity.action.verification.Configuration;

public class NumberRangeConfig extends Configuration {

    public final static int CONFIGURATION_NUMBER_RANGE_TYPE = 2;

    public NumberRangeConfig() {
        super();
        type = CONFIGURATION_NUMBER_RANGE_TYPE;
    }

    public static class Boundary {
        private double value;
        private boolean inclusive;

        public double getValue() {
            return value;
        }

        public boolean isInclusive() {
            return inclusive;
        }
    }

    private Boundary upperBound;
    private Boundary lowerBound;

    public Boundary getUpperBound() {
        return upperBound;
    }

    public Boundary getLowerBound() {
        return lowerBound;
    }

    @Override
    public boolean isValid() {
        if ((upperBound == null) && (lowerBound == null)) {
            return false;
        }
        if ((upperBound != null) && (lowerBound != null) && (lowerBound.value > upperBound.value)) {
            return false;
        }
        return super.isValid();
    }
}

