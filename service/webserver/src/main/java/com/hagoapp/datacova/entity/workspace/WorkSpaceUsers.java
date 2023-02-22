/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.workspace;

import com.hagoapp.datacova.user.UserInfo;

import java.util.*;

/**
 * User list and their roles of a workspace.
 */
public class WorkSpaceUsers {

    private final Map<UserInfo, Set<WorkSpaceUserRole>> map = new HashMap<>();

    public void addUser(UserInfo userInfo, WorkSpaceUserRole role) {
        if (map.containsKey(userInfo)) {
            map.get(userInfo).add(role);
        } else {
            map.put(userInfo, new HashSet<>(Set.of(role)));
        }
    }

    public void addUser(UserInfo userInfo, List<WorkSpaceUserRole> roles) {
        roles.forEach(role -> addUser(userInfo, role));
    }

    public Set<WorkSpaceUserRole> getUserRoles(UserInfo userInfo) {
        return map.get(userInfo);
    }

    public Set<UserInfo> getUsers() {
        return map.keySet();
    }
}
