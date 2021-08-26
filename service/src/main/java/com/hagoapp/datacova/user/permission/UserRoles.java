/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user.permission;

import com.hagoapp.datacova.user.UserInfo;

import java.util.HashSet;
import java.util.Set;

public class UserRoles {
    private UserInfo userInfo;
    private final Set<Role> roles = new HashSet<>();

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public Set<Role> getRoles() {
        return roles;
    }
}
