/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.surveyor;

import com.hagoapp.surveyor.RuleConfig;
import com.hagoapp.surveyor.rule.RegexRuleConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Pattern;

public class RegexRuleSurveyor implements Surveyor {

    private RegexRuleConfig config;
    private Pattern pattern;

    @Override
    public String getSupportedConfigType() {
        return RegexRuleConfig.REGEX_RULE_CONFIG_TYPE;
    }

    @Override
    public Surveyor acceptConfiguration(@NotNull RuleConfig ruleConfig) {
        if (!(ruleConfig instanceof RegexRuleConfig)) {
            throw new IllegalArgumentException("Not a regex rule config");
        }
        config = (RegexRuleConfig) ruleConfig;
        var caseFlag = config.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE;
        pattern = Pattern.compile(config.getPattern(), caseFlag);
        return this;
    }

    @Override
    public boolean process(@NotNull List<Object> params) {
        if (params.isEmpty()) {
            if (config.isNullable()) {
                return true;
            }
            throw new IllegalArgumentException("input must not be null");
        }
        var m = pattern.matcher(params.get(0).toString());
        return m.matches();
    }

}
