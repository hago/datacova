/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.user

import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.permission.UserPermissions

class PermissionCache {
    companion object {

        private const val USER_PERMISSIONS = "USER_PERMISSIONS"
        private const val PERMISSION_EXPIRY = 30 * 60

        fun getUserPermissions(userInfo: UserInfo): UserPermissions {
            val up = RedisCacheReader.readCachedData(
                USER_PERMISSIONS,
                PERMISSION_EXPIRY,
                object : RedisCacheReader.GenericLoader<UserPermissions> {
                    override fun perform(vararg params: Any?): UserPermissions {
                        val u = params[0] as UserInfo
                        return PermissionData().getUserPermissions(u)
                    }

                },
                UserPermissions::class.java,
                userInfo
            )
            return up!!
        }
    }
}
