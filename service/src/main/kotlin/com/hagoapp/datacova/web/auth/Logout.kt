/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web.auth

import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.util.web.AuthUtils
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.WebInterface
import io.netty.handler.codec.http.HttpResponseStatus

class Logout : WebInterface {

    override fun requestHandlers(): Map<String, WebInterface.Handler> {
        return mapOf(
            MethodName.GET to WebInterface.Handler { context ->
                AuthUtils.clearImpersonator(context)
                AuthUtils.logoutCurrentUser(context)
                ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0))
            }
        )
    }

    override fun getPath(): String {
        return "/api/auth/logout"
    }

}