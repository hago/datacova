/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.user

import com.google.gson.Gson
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.data.RedisCacheReader.GenericLoader
import com.hagoapp.datacova.data.redis.JedisManager
import com.hagoapp.datacova.user.UserInfo

class UserCache {
    companion object {

        private const val USER_INFO = "UserInfo"
        private const val USER_INFO_CACHE_TIME = 3600

        @JvmStatic
        fun getUser(id: Long): UserInfo? {
            return RedisCacheReader.readCachedData(USER_INFO, USER_INFO_CACHE_TIME, object : GenericLoader<UserInfo> {
                override fun perform(params: Array<out Any?>): UserInfo? {
                    UserData().use {
                        return it.findUser(params[0] as Long)
                    }
                }
            }, UserInfo::class.java, id)
        }

        @JvmStatic
        fun batchGetUser(idList: List<Long>): List<UserInfo?> {
            val list = idList.map { id ->
                RedisCacheReader.readCachedData(USER_INFO, 3600, object : GenericLoader<UserInfo> {
                    override fun perform(params: Array<out Any?>): UserInfo? {
                        return null
                    }
                }, UserInfo::class.java, id)
            }.toMutableList()
            val nullUsers = list.mapIndexed { i, info ->
                if (info == null) Pair(idList[i], i) else null
            }.filterNotNull().toMap()
            JedisManager(CoVaConfig.getConfig().redis).use {
                val jedis = it.jedis
                val gson = Gson()
                UserData().batchGetUser(nullUsers.keys).forEach { info ->
                    if (info != null) {
                        val index = nullUsers.getValue(info.id)
                        list[index] = info
                        val key = RedisCacheReader.createCacheKey(USER_INFO, info.id)
                        jedis.setex(key, USER_INFO_CACHE_TIME, gson.toJson(info))
                    }
                }
            }
            return list
        }
    }
}
