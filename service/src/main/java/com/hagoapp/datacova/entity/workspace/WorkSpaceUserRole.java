/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.workspace;

import com.google.gson.annotations.SerializedName;

public enum WorkSpaceUserRole {
    @SerializedName("0")
    Admin(0),
    @SerializedName("1")
    Maintainer(1),
    @SerializedName("2")
    User(2);

    private final int value;

    public int getValue() {
        return value;
    }

    WorkSpaceUserRole(int i) {
        value = i;
    }

    public static WorkSpaceUserRole parseInt(int i) {
        for (WorkSpaceUserRole u : WorkSpaceUserRole.values()) {
            if (i == u.value) {
                return u;
            }
        }
        return WorkSpaceUserRole.User;
    }
}
