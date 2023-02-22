/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket.timing

import com.hagoapp.datacova.web.websocket.ClientMessage

class TimingClientMessage : ClientMessage() {

    companion object {
        const val TIMING_CLIENT_MESSAGE_TYPE = 1
    }

    init {
        messageType = TIMING_CLIENT_MESSAGE_TYPE
    }
}