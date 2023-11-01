/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.execution;

public class DataMessage {
    private String field;
    private Object value;
    private String descriptionExpected;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDescriptionExpected() {
        return descriptionExpected;
    }

    public void setDescriptionExpected(String descriptionExpected) {
        this.descriptionExpected = descriptionExpected;
    }

    @Override
    public String toString() {
        return "DataMessage{" +
                "field='" + field + '\'' +
                ", value=" + value +
                ", descriptionExpected='" + descriptionExpected + '\'' +
                '}';
    }
}
