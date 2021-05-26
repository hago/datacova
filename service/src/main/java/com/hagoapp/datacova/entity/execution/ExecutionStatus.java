/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.execution;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ExecutionStatus {
    @SerializedName("0")
    added(0),
    @SerializedName("1")
    executing(1),
    @SerializedName("2")
    succeed(2),
    @SerializedName("-1")
    fail(-1),
    @SerializedName("-2")
    FileUploaded(-2);

    private final int value;

    ExecutionStatus(int v) {
        this.value = v;
    }

    public static ExecutionStatus valueOf(int v) throws InstantiationException {
        List<ExecutionStatus> l = Arrays.stream(values()).filter(e -> e.value == v).collect(Collectors.toList());
        if (l.size() > 0) {
            return l.get(0);
        } else {
            throw new InstantiationException(String.format("invalid value %d for ExecutionStatus enum", v));
        }
    }

    public int getValue() {
        return this.value;
    }
}
