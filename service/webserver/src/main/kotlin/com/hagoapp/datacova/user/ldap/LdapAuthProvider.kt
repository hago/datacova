/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user.ldap

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.utility.ldap.LdapAttributeNames
import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.data.user.UserData
import com.hagoapp.datacova.utility.ldap.LdapHelper
import com.hagoapp.datacova.user.UserAuthProvider
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.UserSearchResultItem
import com.hagoapp.datacova.user.UserType
import io.vertx.ext.web.RoutingContext

class LdapAuthProvider : UserAuthProvider {
    companion object {
        const val PROVIDER_NAME = "ldap"
        private const val USERNAME_FIELD = "userId"
        private const val PASSWORD_FIELD = "password"
        const val LDAP_USER_TYPE = 1
    }

    private fun createLdap(): LdapHelper {
        return LdapHelper(LdapConfigManager.defaultConfig)
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
            LdapHelper(LdapConfigManager.defaultConfig).use {
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
            userId = map.getValue(config.attributeNames.getActualAttribute(LdapAttributeNames.ATTRIBUTE_USERID)).toString()
            provider = getProviderName()
            userType = UserType.parseInt(getProviderType())
            name = map.getValue(config.attributeNames.getActualAttribute(LdapAttributeNames.ATTRIBUTE_DISPLAY_NAME)).toString()
            email = map.getValue(config.attributeNames.getActualAttribute(LdapAttributeNames.ATTRIBUTE_MAIL)).toString()
            mobile =
                map.getValue(config.attributeNames.getActualAttribute(LdapAttributeNames.ATTRIBUTE_TELEPHONE_NUMBER)).toString()
            pwdHash = ""
        }
        return UserData(CoVaConfig.getConfig().database).addUserFromProvider(userInfo).id
    }

    override fun loadThumbnail(userInfo: UserInfo) {
        // no extra job is needed to load thumbnail from ldap
    }

    override fun saveThumbnail(userId: String, thumbnail: ByteArray): String {
        throw UnsupportedOperationException("Update thumbnail is not supported")
    }

    override fun searchUser(search: String, count: Int): List<UserSearchResultItem> {
        createLdap().use { ldap ->
            val attributes = LdapConfigManager.defaultConfig!!.attributeNames
            val cn = attributes.getActualAttribute(LdapAttributeNames.ATTRIBUTE_USERID)
            val name = attributes.getActualAttribute(LdapAttributeNames.ATTRIBUTE_DISPLAY_NAME)
            val mobile = attributes.getActualAttribute(LdapAttributeNames.ATTRIBUTE_TELEPHONE_NUMBER)
            val email = attributes.getActualAttribute(LdapAttributeNames.ATTRIBUTE_MAIL)
            val l = ldap.searchUser(search, listOf(cn, name, mobile, email), count)
            return l.map { item ->
                UserSearchResultItem(
                    item.getValue(cn).toString(),
                    null,
                    item.getValue(name).toString(),
                    getProviderType(),
                    item.getValue(email).toString(),
                    item.getValue(mobile)?.toString() ?: ""
                )
            }
        }
    }
}
