/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.user;

import com.google.gson.GsonBuilder;

public class UserInfo {
    private String userId;
    private String provider;

    public UserInfo() {

    }

    public UserInfo(String userId, String provider) {
        this.userId = userId;
        this.provider = provider;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserInfo userInfo = (UserInfo) o;

        if (!userId.equals(userInfo.userId)) return false;
        return provider.equals(userInfo.provider);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + provider.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new GsonBuilder().serializeNulls().create().toJson(this);
    }
}
