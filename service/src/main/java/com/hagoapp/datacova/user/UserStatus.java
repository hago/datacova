/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user;

import com.google.gson.annotations.SerializedName;

public enum UserStatus {
    @SerializedName("0")
    Normal(0),
    @SerializedName("1")
    Deleted(1),
    @SerializedName("2")
    PasswordReset(2);

    private final int value;

    UserStatus(int i) {
        value = i;
    }

    public static UserStatus parseInt(int i) {
        for (UserStatus u: UserStatus.values()) {
            if (i == u.value) {
                return u;
            }
        }
        return UserStatus.Normal;
    }
}
