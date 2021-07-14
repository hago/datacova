/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user.ldap

import com.hagoapp.datacova.config.indb.LdapAttributes
import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.data.user.UserData
import com.hagoapp.datacova.user.UserAuthProvider
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.UserType
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

    private fun createLdap(): LdapUtils {
        return LdapUtils(LdapConfigManager.defaultConfig)
    }

    override fun authenticate(context: RoutingContext): UserInfo? {
        val ldap = createLdap()
        val userId = context.request().getParam(USERNAME_FIELD)
        val userName = ldap.createDistinguishedName(userId)
        val password = context.request().getParam(PASSWORD_FIELD)
        ldap.use {
            if (!it.bind(userName, password)) {
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
        val u = UserCache.getUserByUserId(userId, getProviderType())
        if (u == null) {
            LdapUtils(LdapConfigManager.defaultConfig).use {
                val map = it.getUser(userId)
                val id = saveUserToDatabase(map)
                return UserCache.getUser(id)
            }
        } else {
            return u
        }
    }

    private fun saveUserToDatabase(map: Map<String, Any?>): Long {
        val userInfo = UserInfo()
        val config = LdapConfigManager.defaultConfig!!
        with(userInfo) {
            userId = map.getValue(config.attributes.getActualAttribute(LdapAttributes.ATTRIBUTE_USERID)).toString()
            provider = getProviderName()
            userType = UserType.parseInt(getProviderType())
            name = map.getValue(config.attributes.getActualAttribute(LdapAttributes.ATTRIBUTE_DISPLAY_NAME)).toString()
            email = map.getValue(config.attributes.getActualAttribute(LdapAttributes.ATTRIBUTE_MAIL)).toString()
            mobile =
                map.getValue(config.attributes.getActualAttribute(LdapAttributes.ATTRIBUTE_TELEPHONE_NUMBER)).toString()
            pwdHash = ""
        }
        return UserData().addUserFromProvider(userInfo).id
    }

    override fun loadThumbnail(userInfo: UserInfo) {
        // no extra job is needed to load thumbnail from ldap
    }

    override fun saveThumbnail(userId: String, thumbnail: ByteArray): String {
        throw UnsupportedOperationException("Update thumbnail is not supported")
    }
}
