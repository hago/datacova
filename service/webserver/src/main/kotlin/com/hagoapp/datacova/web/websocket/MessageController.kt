/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket

import com.google.gson.Gson
import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext

class MessageController {
    @WebEndPoint(
        path = "/api/ws/send",
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun sendMessage(context: RoutingContext) {
        val str = context.body().asString()
        val msg = Message.fromString(str)
        val recipient = UserCache.getUserByUserId(msg.recipient, msg.recipientType)
        if (recipient == null) {
            ResponseHelper.respondError(
                context,
                HttpResponseStatus.NOT_FOUND,
                "User ${msg.recipient} from provider ${msg.recipientType} Not Found"
            )
            return
        }
        val ws = WebSocketManager.getManager()
        ws.sendUserMessage(msg.body, recipient)
    }

    data class Message(
        //val sender: String,
        //val senderType: Int,
        val recipient: String,
        val recipientType: Int,
        val body: String
    ) {
        companion object {
            fun fromString(input: String): Message {
                return Gson().fromJson(input, Message::class.java)
            }
        }
    }
}
