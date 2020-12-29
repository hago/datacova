/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.action.validate;

import com.hagoapp.datacova.JsonStringify;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ValidateConfig implements JsonStringify {
    private ValidateType type;
    private List<String> fields = new ArrayList<>();
    private boolean nullable = false;
    private boolean ignoreFieldCase = false;
    private int FieldsCountLimit = 1;

    public ValidateType getType() {
        return type;
    }

    public void setType(ValidateType type) {
        this.type = type;
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

    public List<String> getFields() {
        return fields;
    }

    public boolean isValid() {
        if ((fields == null) || (fields.size() == 0)) {
            return false;
        }
        List<String> distinct = ignoreFieldCase ?
                fields.stream().map(s -> s.toLowerCase()).distinct().collect(Collectors.toList())
                : fields.stream().distinct().collect(Collectors.toList());
        if (distinct.size() != fields.size()) {
            return false;
        }
        if (FieldsCountLimit > 0) {
            fields = fields.subList(0, FieldsCountLimit);
        }
        return true;
    }
}
