/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.authentication.usertoken;

import com.hagoapp.datacova.user.UserInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UserTokenStoreMemory implements UserTokenStore{

    private final Map<String, UserInfo> tokenUserMap = new ConcurrentHashMap<>();
    private final Map<UserInfo, Set<String>> userTokensMap = new ConcurrentHashMap<>();

    @Override
    public void storeUserToken(String token, UserInfo userInfo) {
        tokenUserMap.put(token, userInfo);
        if (userTokensMap.containsKey(userInfo)) {
            userTokensMap.get(userInfo).add(token);
        } else {
            userTokensMap.put(userInfo, new HashSet<>(Set.of(token)));
        }
    }

    @Override
    public void removeUserToken(String token) {
        UserInfo userInfo = tokenUserMap.remove(token);
        if (userInfo != null) {
            userTokensMap.computeIfPresent(userInfo, (key, tokenSet) -> {
                tokenSet.remove(token);
                return tokenSet;
            });
        }
    }

    @Override
    public UserInfo findUserToken(String token) {
        return tokenUserMap.get(token);
    }

    @Override
    public Set<String> findTokensOfUser(UserInfo userInfo) {
        return userTokensMap.get(userInfo);
    }
}
