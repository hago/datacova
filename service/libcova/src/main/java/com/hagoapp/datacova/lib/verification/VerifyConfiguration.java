/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.verification;

import com.google.gson.Gson;
import com.hagoapp.datacova.surveyor.JsonStringify;
import com.hagoapp.datacova.surveyor.RuleConfig;
import com.hagoapp.datacova.surveyor.SurveyorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The configuration file for verification action.
 *
 * @author Chaojun Sun
 * @since 0.1
 */
public class VerifyConfiguration implements JsonStringify {
    private List<String> fields = new ArrayList<>();
    private boolean nullable = false;
    private boolean ignoreFieldCase = false;
    private int FieldsCountLimit = 1;
    private RuleConfig ruleConfig;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isIgnoreFieldCase() {
        return ignoreFieldCase;
    }

    public void setIgnoreFieldCase(boolean ignoreFieldCase) {
        this.ignoreFieldCase = ignoreFieldCase;
    }

    public int getFieldsCountLimit() {
        return FieldsCountLimit;
    }

    public void setFieldsCountLimit(int fieldsCountLimit) {
        FieldsCountLimit = fieldsCountLimit;
    }

    public RuleConfig getRuleConfig() {
        return ruleConfig;
    }

    public void setRuleConfig(RuleConfig ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public boolean isValid() {
        if (fields.isEmpty()) {
            return false;
        }
        long distinctSize = ignoreFieldCase ?
                fields.stream().map(String::toLowerCase).distinct().count()
                : fields.stream().distinct().count();
        if (distinctSize != fields.size()) {
            return false;
        }
        if (FieldsCountLimit > 0) {
            fields = fields.subList(0, FieldsCountLimit);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static VerifyConfiguration create(Map<String, Object> source) {
        if (!source.containsKey("ruleConfig")) {
            throw new UnsupportedOperationException("No 'RuleConfig' defined");
        }
        var item = source.get("ruleConfig");
        if (!(item instanceof Map<?, ?>)) {
            throw new UnsupportedOperationException("'RuleConfig' not valid");
        }
        var subMap = (Map<String, Object>) item;
        var gson = new Gson();
        var conf = gson.fromJson(gson.toJson(source), VerifyConfiguration.class);
        var ruleConfig = SurveyorFactory.createRuleConfig(subMap);
        conf.setRuleConfig(ruleConfig);
        return conf;
    }
}
