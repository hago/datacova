/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.authentication.usertoken;

import com.hagoapp.datacova.user.UserInfo;

import java.util.Set;

public interface UserTokenStore {
    void storeUserToken(String token, UserInfo userInfo);

    void removeUserToken(String token);

    UserInfo findUserToken(String token);

    Set<String> findTokensOfUser(UserInfo userInfo);
}
