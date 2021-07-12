/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.default

import com.hagoapp.datacova.data.setting.SettingsDatabase
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
        val ldap = SettingsDatabase().loadLdapConfig()
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf(
            "code" to 0,
            "settings" to {
                "user" to mapOf(
                    "ldap" to ldap
                )
            }
        ))
    }
}
