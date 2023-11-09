/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility

import com.hagoapp.datacova.utility.ldap.LdapAttributeNames
import com.hagoapp.datacova.utility.ldap.LdapConfig
import com.hagoapp.datacova.utility.ldap.LdapHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperties
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.slf4j.LoggerFactory

@EnabledIfSystemProperties(
    EnabledIfSystemProperty(named = LdapTest.HOST, matches = ".*"),
    EnabledIfSystemProperty(named = LdapTest.BASE_DN, matches = ".*"),
    EnabledIfSystemProperty(named = LdapTest.BIND_DN, matches = ".*"),
    EnabledIfSystemProperty(named = LdapTest.BIND_PWD, matches = ".*")
)
class LdapTest {

    companion object {
        const val HOST = "ldap.host"
        private const val PORT = "ldap.port"
        const val BASE_DN = "ldap.basedn"
        const val BIND_DN = "ldap.user"
        const val BIND_PWD = "ldap.pwd"
        private const val USER_DN_PATTERN = "ldap.userdn"

        private val config = LdapConfig()
        private val logger = LoggerFactory.getLogger(LdapTest::class.java)

        @BeforeAll
        @JvmStatic
        fun init() {
            config.host = System.getProperty(HOST)
            config.port = System.getProperty(PORT)?.toInt() ?: 389
            config.bindDistinguishName = System.getProperty(BIND_DN)
            config.bindPassword = System.getProperty(BIND_PWD)
            config.baseDistinguishName = System.getProperty(BASE_DN)
            config.attributeNames = LdapAttributeNames.defaultAttributes()
            logger.debug("config: {}", config)
            if (System.getProperty(USER_DN_PATTERN) != null) {
                config.userDnPattern = System.getProperty(USER_DN_PATTERN)
            }
        }
    }

    @Test
    fun testFindUser() {
        LdapHelper(config).use {
            val dnName = config.attributeNames.getActualAttribute(LdapAttributeNames.ATTRIBUTE_DISTINGUISHED_NAME)
            val cnName = config.attributeNames.getActualAttribute(LdapAttributeNames.ATTRIBUTE_USERID)
            val users = it.search("($dnName=${config.bindDistinguishName})")
            Assertions.assertFalse(users.isEmpty())
            val u = users[0]
            val cn = u[cnName] as String
            val user = it.getUser(cn)
            Assertions.assertTrue(user.containsKey(cnName))
            Assertions.assertEquals(cn, user[cnName])
        }
    }
}
