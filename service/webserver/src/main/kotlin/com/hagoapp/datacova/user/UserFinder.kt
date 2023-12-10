/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.user.UserData

class UserFinder {
    companion object {
        fun search(searchReq: UserSearchReq): UserSearchResult {
            val users = UserData(CoVaConfig.getConfig().database).searchUser(searchReq.search, searchReq.userProviders, searchReq.count).map {
                UserSearchResultItem.fromUserInfo(it)
            }
            val other = mutableMapOf<Int, List<UserSearchResultItem>>()
            searchReq.userProviders.forEach { providerType ->
                val provider = UserAuthFactory.getAuthProvider(providerType)
                val alreadyFound = users.filter { it.provider == providerType }.map { Pair(it.userId, 1) }.toMap()
                val list = provider.searchUser(searchReq.search, searchReq.count + alreadyFound.size).filter {
                    !alreadyFound.containsKey(it.userId)
                }.take(searchReq.count)
                other[providerType] = list
            }
            return UserSearchResult(
                users,
                other
            )
        }
    }
}
