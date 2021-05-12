/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.verification.conf;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.entity.action.verification.Configuration;
import com.hagoapp.datacova.util.text.TextResourceManager;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Locale;

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

    @Override
    protected String createDescription(Locale locale) throws CoVaException {
        String format = TextResourceManager.getManager().getString(locale, "/validators/number_range");
        if (format == null) {
            throw new CoVaException("Description for NumberRangeConfig class not found");
        }
        List<String> fields = getFields();
        if (fields.size() == 0) {
            throw new CoVaException("No fields defined in NumberRangeConfig class");
        }
        ST st = new ST(format);
        st.add("fields", fields);
        st.add("lowerBound", lowerBound);
        st.add("upperBound", upperBound);
        return st.render();
    }
}

