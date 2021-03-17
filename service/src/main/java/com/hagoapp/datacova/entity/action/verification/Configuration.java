/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.verification;

import com.google.gson.GsonBuilder;

import java.util.List;
import java.util.stream.Collectors;

public class Configuration {
    public int type;
    public List<String> fields;
    public boolean nullable = false;
    public boolean ignoreFieldCase = false;

    public int FieldsCountLimit = 1;

    public boolean isNullable() {
        return nullable;
    }

    public String toString() {
        return new GsonBuilder().create().toJson(this);
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
