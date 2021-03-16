/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.user

import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.data.RedisCacheReader.GenericLoader
import com.hagoapp.datacova.data.UserData
import com.hagoapp.datacova.user.UserInfo

class UserCache {
    companion object {

        private const val USER_INFO = "UserInfo"

        @JvmStatic
        fun getUser(id: Long): UserInfo? {
            return RedisCacheReader.readCachedData(USER_INFO, 3600, object : GenericLoader<UserInfo> {
                override fun perform(params: Array<out Any?>): UserInfo? {
                    UserData().use {
                        return it.findUser(params[0] as Long)
                    }
                }
            }, UserInfo::class.java, id)
        }
    }
}
