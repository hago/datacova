/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user

data class UserSearchResultItem(
    val userId: String,
    val id: Long?,
    val name: String,
    val provider: Int
) {
    companion object {
        fun fromUserInfo(userInfo: UserInfo): UserSearchResultItem {
            return UserSearchResultItem(
                userInfo.userId,
                userInfo.id,
                userInfo.name,
                userInfo.userType.value
            )
        }
    }
}
