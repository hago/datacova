/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.data.UserData

class LocalUserProvider : UserAuthProvider {

    companion object {
        const val PROVIDER_NAME = "local"
    }

    private val dbConfig = CoVaConfig.getConfig().database

    override fun authenticate(userId: String, vararg credentials: String): Boolean {
        if (credentials.size != 1) {
            return false
        }
        val userInfo = getUserInfo(userId)
        val hash = UserData.computePwdHash(credentials[0])
        return (userInfo != null) && (userInfo.pwdHash == hash)
    }

    override fun getProviderName(): String {
        return PROVIDER_NAME
    }

    override fun getUserInfo(userId: String): UserInfo? {
        return RedisCacheReader.readCachedData("UserInfo", 60 * 30,
            userInfoLoader, UserInfo::class.java, userId)
    }

    private val userInfoLoader = object: RedisCacheReader.GenericLoader<UserInfo> {
        override fun perform(vararg params: Any?): UserInfo? {
            UserData(dbConfig).use { return it.findUser(params[0] as String) }
        }

    }
}