/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.surveyor.rule;

import com.hagoapp.datacova.surveyor.RuleConfig;

/**
 * The configuration for a regex matching rule.
 *
 * @author Chaojun Sun
 * @since 0.1
 */
public class RegexRuleConfig extends RuleConfig {
    /**
     * The regex surveyor type identity.
     */
    public static final String REGEX_RULE_CONFIG_TYPE = "com.hagoapp.regex";
    private String pattern;
    private boolean caseSensitive;

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    @Override
    public String getConfigType() {
        return REGEX_RULE_CONFIG_TYPE;
    }
}
