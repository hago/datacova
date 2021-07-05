/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.setting

import com.hagoapp.datacova.config.indb.LdapConfig
import com.hagoapp.datacova.config.init.CoVaConfig
import com.hagoapp.datacova.config.init.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.user.ldap.LdapConfigManager

class SettingsDatabase(connectionConfig: DatabaseConfig) : CoVaDatabase(connectionConfig) {
    constructor() : this(CoVaConfig.getConfig().database)

    companion object {
        const val CONFIGURATION_ITEM_LDAP = "ldap"
    }

    fun loadLdapConfig(): LdapConfig? {
        val sql = "select * from settings where name = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, CONFIGURATION_ITEM_LDAP)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) {
                    val s = rs.getString("content")
                    LdapConfigManager.getConfig(s)
                } else {
                    null
                }
            }
        }
    }
}
