/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.user

import com.google.gson.Gson
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.utility.redis.JedisManager
import com.hagoapp.datacova.utility.redis.RedisCacheReader

class UserCache {
    companion object {

        private const val USER_INFO = "UserInfo"
        private const val USER_INFO_BY_USERID = "UserInfoUserId"
        private const val USER_INFO_CACHE_TIME = 3600L
        private const val USER_REGISTRATION_CODE_KEY_PREFIX = "UserRegistrationActivation"

        @JvmStatic
        fun getUser(id: Long): UserInfo? {
            return RedisCacheReader.readCachedData(
                CoVaConfig.getConfig().redis,
                USER_INFO, USER_INFO_CACHE_TIME, { params ->
                    UserData(CoVaConfig.getConfig().database).use {
                        it.findUser(params[0] as Long)
                    }
                }, null, UserInfo::class.java, id
            )
        }

        @JvmStatic
        fun batchGetUser(idList: List<Long>): List<UserInfo?> {
            val list = idList.map { id ->
                RedisCacheReader.readCachedData<UserInfo?>(
                    CoVaConfig.getConfig().redis,
                    USER_INFO, 3600,
                    { null }, null, UserInfo::class.java, id
                )
            }.toMutableList()
            val nullUsers = list.mapIndexed { i, info ->
                if (info == null) Pair(idList[i], i) else null
            }.filterNotNull().toMap()
            JedisManager.getJedis(CoVaConfig.getConfig().redis).use { jedis ->
                val gson = Gson()
                UserData(CoVaConfig.getConfig().database).batchGetUser(nullUsers.keys).forEach { info ->
                    if (info != null) {
                        val key = RedisCacheReader.createCacheKey(USER_INFO, info.id)
                        jedis.setex(key, USER_INFO_CACHE_TIME, gson.toJson(info))
                    }
                }
            }
            return list
        }

        @JvmStatic
        fun saveUserRegistrationCode(userInfo: UserInfo, code: String, expireSecond: Long) {
            JedisManager.getJedis(CoVaConfig.getConfig().redis).use {
                val key = "$USER_REGISTRATION_CODE_KEY_PREFIX|$code"
                it.setex(key, expireSecond, userInfo.id.toString())
            }
        }

        @JvmStatic
        fun getUserIdByRegistrationCode(code: String): Long? {
            JedisManager.getJedis(CoVaConfig.getConfig().redis).use {
                val key = "$USER_REGISTRATION_CODE_KEY_PREFIX|$code"
                return it.get(key)?.toLong()
            }
        }

        @JvmStatic
        fun getUserByUserId(userId: String, userType: Int): UserInfo? {
            return RedisCacheReader.readCachedData(
                CoVaConfig.getConfig().redis,
                USER_INFO_BY_USERID,
                USER_INFO_CACHE_TIME,
                { params ->
                    val uid = params[0].toString()
                    val type = params[1] as Int
                    UserData(CoVaConfig.getConfig().database).findUser(uid, type)
                },
                null,
                UserInfo::class.java,
                userId,
                userType
            )
        }
    }
}
