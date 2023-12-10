/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user;

import com.google.gson.annotations.SerializedName;

public enum UserType {
    @SerializedName("0")
    REGISTERED(0),
    @SerializedName("1")
    LDAP(1);

    private final int value;

    public int getValue() {
        return value;
    }

    UserType(int i) {
        value = i;
    }

    public static UserType parseInt(int i) {
        for (UserType u : UserType.values()) {
            if (i == u.value) {
                return u;
            }
        }
        return UserType.REGISTERED;
    }
}
