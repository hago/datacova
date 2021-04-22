/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.action.verification.conf;

import com.hagoapp.datacova.entity.action.verification.Configuration;

import java.util.ArrayList;
import java.util.List;

public class OptionsConfig extends Configuration {
    private static final int OPTIONS_CONFIG_TYPE = 3;
    private final List<String> options = new ArrayList<>();
    private boolean ignoreCase = false;
    private boolean allowEmpty = false;

    public OptionsConfig() {
        type = OPTIONS_CONFIG_TYPE;
    }

    public List<String> getOptions() {
        return options;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    public boolean isAnOption(String s) {
        if ((s == null) || allowEmpty) {
            return options.contains(null);
        }
        if (!ignoreCase) {
            return this.options.contains(s);
        } else {
            return this.options.stream().anyMatch(item -> item != null && item.compareToIgnoreCase(s) == 0);
        }
    }

    @Override
    public boolean isValid() {
        if (options.size() == 0) {
            return false;
        }
        long uniqueSize = ignoreCase ?
                options.stream().map(String::toLowerCase).distinct().count() :
                options.stream().distinct().count();
        return uniqueSize == options.size() && super.isValid();
    }
}
