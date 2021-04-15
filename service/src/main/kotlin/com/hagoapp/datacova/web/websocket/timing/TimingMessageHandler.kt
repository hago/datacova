/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket.timing

import com.hagoapp.datacova.web.websocket.ClientMessage
import com.hagoapp.datacova.web.websocket.IMessageHandler
import com.hagoapp.datacova.web.websocket.ServerMessage
import com.hagoapp.datacova.web.websocket.timing.TimingClientMessage.Companion.TIMING_CLIENT_MESSAGE_TYPE
import io.vertx.core.http.ServerWebSocket
import java.lang.UnsupportedOperationException

class TimingMessageHandler : IMessageHandler {
    override fun getHandledMessageTypes(): MutableList<Int> {
        return mutableListOf(TIMING_CLIENT_MESSAGE_TYPE)
    }

    override fun handleMessage(serverWebSocket: ServerWebSocket, message: ClientMessage): ServerMessage {
        return TimingServerMessage();
    }

    override fun getMessageType(type: Int): Class<out ClientMessage> {
        return when (type) {
            TIMING_CLIENT_MESSAGE_TYPE -> TimingClientMessage::class.java
            else -> throw UnsupportedOperationException("Not an timing message $type")
        }
    }
}