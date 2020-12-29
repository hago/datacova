package com.hagoapp.datacova.user;

import com.google.gson.annotations.SerializedName;

public enum UserStatus {
    @SerializedName("0")
    Normal(0),
    @SerializedName("1")
    Deleted(1),
    @SerializedName("2")
    PasswordReset(2);

    private int value;

    UserStatus(int i) {
        value = i;
    }
}
