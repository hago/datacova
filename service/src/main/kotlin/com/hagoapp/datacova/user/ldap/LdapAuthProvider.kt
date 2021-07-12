/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user.ldap

import com.hagoapp.datacova.config.indb.LdapConfig
import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.user.UserAuthProvider
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.util.ldap.LdapUtils
import io.vertx.ext.web.RoutingContext
import java.lang.UnsupportedOperationException

class LdapAuthProvider : UserAuthProvider {
    companion object {
        const val PROVIDER_NAME = "ldap"
        private const val USERNAME_FIELD = "userId"
        private const val PASSWORD_FIELD = "password"
        const val LDAP_USER_TYPE = 1
    }

    var config: LdapConfig? = null

    private fun createLdap(): LdapUtils? {
        return if (config == null) null
        else LdapUtils(config)
    }

    override fun authenticate(context: RoutingContext): UserInfo? {
        val ldap = createLdap() ?: return null
        val userId = context.request().getParam(USERNAME_FIELD)
        val userName = createUserDn(userId)
        val password = context.request().getParam(PASSWORD_FIELD)
        ldap.use {
            if (it.bind(userName, password)) {
                return null
            }
        }
        return getUserInfo(userId)
    }

    override fun getProviderName(): String {
        return PROVIDER_NAME
    }

    override fun getProviderType(): Int {
        return LDAP_USER_TYPE
    }

    override fun getUserInfo(userId: String): UserInfo? {
        if (config == null) {
            return null
        }
        val u = UserCache.getUserByUserId(userId, getProviderType())
        if (u == null) {
            LdapUtils(config).use {
                val map = it.getUser(userId)
                val id = saveUserToDatabase(map)
                return UserCache.getUser(id)
            }
        } else {
            return u
        }
    }

    private fun createUserDn(userId: String): String {
        return String.format(config!!.userDnPattern, userId)
    }

    private fun saveUserToDatabase(map: Map<String, Any?>): Long {
        TODO()
    }

    override fun loadThumbnail(userInfo: UserInfo) {
        // no extra job is needed to load thumbnail from ldap
    }

    override fun saveThumbnail(userId: String, thumbnail: ByteArray): String {
        throw UnsupportedOperationException("Update thumbnail is not supported")
    }
}
