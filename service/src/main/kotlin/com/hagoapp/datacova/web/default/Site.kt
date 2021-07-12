/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.default

import com.hagoapp.datacova.data.setting.SettingsDatabase
import com.hagoapp.datacova.user.ldap.LdapAuthProvider
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class Site {
    @WebEndPoint(
        path = "/api/default/settings",
        methods = [HttpMethod.GET]
    )
    fun settings(context: RoutingContext) {
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "settings" to mapOf(
                    "userProviders" to loadUserProviders()
                )
            )
        )
    }

    private fun loadUserProviders(): List<UserProvider> {
        val userProviders = mutableListOf<UserProvider>()
        if (SettingsDatabase().loadLdapConfig() != null) {
            val ldapProvider = LdapAuthProvider()
            userProviders.add(UserProvider(ldapProvider.getProviderName(), ldapProvider.getProviderType()))
        }
        return userProviders
    }

    data class UserProvider(
        val name: String,
        val providerType: Int
    )
}
