/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user.ldap

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hagoapp.datacova.config.ldap.LdapConfig
import com.hagoapp.datacova.data.setting.SettingsDatabase

class LdapConfigManager {
    companion object {
        var defaultConfig: LdapConfig? = SettingsDatabase().loadLdapConfig()

        fun isLdapEnabled(): Boolean {
            return defaultConfig != null
        }

        fun getConfig(content: String): LdapConfig? {
            return try {
                val conf = Gson().fromJson(content, LdapConfig::class.java)
                conf.attributes.normalize()
                conf
            } catch (e: JsonSyntaxException) {
                null
            }
        }
    }
}
