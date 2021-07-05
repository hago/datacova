/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user.ldap

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hagoapp.datacova.config.indb.LdapConfig

class LdapConfigManager {
    companion object {
        private var defaultConfig: LdapConfig? = null
        private var initialized = false
        fun getDefault(): LdapConfig? {
            if (!initialized) {
                initialized = true
                TODO()
            }
            return defaultConfig
        }

        fun isLdapEnabled(): Boolean {
            return getDefault() != null
        }

        fun getConfig(content: String): LdapConfig? {
            return try {
                Gson().fromJson(content, LdapConfig::class.java)
            } catch (e: JsonSyntaxException) {
                null
            }
        }
    }
}
