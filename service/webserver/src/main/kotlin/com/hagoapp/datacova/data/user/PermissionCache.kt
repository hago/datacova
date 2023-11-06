/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.user

import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.permission.UserPermissions
import com.hagoapp.datacova.utility.redis.RedisCacheReader

class PermissionCache {
    companion object {

        private const val USER_PERMISSIONS = "USER_PERMISSIONS"
        private const val PERMISSION_EXPIRY = 30 * 60L

        fun getUserPermissions(userInfo: UserInfo): UserPermissions {
            val up = RedisCacheReader.readCachedData(
                USER_PERMISSIONS,
                PERMISSION_EXPIRY,
                { params ->
                    val u = params[0] as UserInfo
                    PermissionData().getUserPermissions(u)
                },
                UserPermissions::class.java,
                userInfo
            )
            return up!!
        }
    }
}
