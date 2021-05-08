/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.verification;

import com.hagoapp.datacova.JsonStringify;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Configuration implements JsonStringify {
    protected int type;
    private List<String> fields = new ArrayList<>();
    private boolean nullable = false;
    private boolean ignoreFieldCase = false;
    private int FieldsCountLimit = 1;

    public boolean isNullable() {
        return nullable;
    }

    public int getType() {
        return type;
    }

    public List<String> getFields() {
        return fields;
    }

    public boolean isIgnoreFieldCase() {
        return ignoreFieldCase;
    }

    public int getFieldsCountLimit() {
        return FieldsCountLimit;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public void setIgnoreFieldCase(boolean ignoreFieldCase) {
        this.ignoreFieldCase = ignoreFieldCase;
    }

    public void setFieldsCountLimit(int fieldsCountLimit) {
        FieldsCountLimit = fieldsCountLimit;
    }

    public String toString() {
        return this.toJson();
    }

    public boolean isValid() {
        if (fields.size() == 0) {
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

    public String describe() {
        return describe(Locale.getDefault());
    }

    public String describe(Locale locale) {
        throw new UnsupportedOperationException("describe operation is not supported in base class of verification classes, override it in your descendant classes.");
    }
}
