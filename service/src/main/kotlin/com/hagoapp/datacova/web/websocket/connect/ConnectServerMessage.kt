/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.hagoapp.datacova.web.websocket.connect

import com.hagoapp.datacova.web.websocket.ServerMessage

class ConnectServerMessage : ServerMessage() {

    companion object {
        const val CONNECT_MESSAGE_TYPE = 0
    }

    init {
        messageType = CONNECT_MESSAGE_TYPE
        id = 0
    }

    val message = "Connected"
}
