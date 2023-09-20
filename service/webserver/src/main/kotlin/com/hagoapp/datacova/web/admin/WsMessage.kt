/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.admin

import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.websocket.WebSocketManager
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext

class WsMessage {
    @WebEndPoint(
        path = "/api/admin/ws/sessions",
        methods = [MethodName.GET],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun getClients(context: RoutingContext) {
        val sessions = WebSocketManager.getManager().allWsSessions
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to sessions
            )
        )
    }
}
