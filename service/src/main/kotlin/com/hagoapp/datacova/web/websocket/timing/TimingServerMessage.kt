/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket.timing

import com.hagoapp.datacova.web.websocket.ServerMessage
import com.hagoapp.datacova.web.websocket.timing.TimingClientMessage.Companion.TIMING_CLIENT_MESSAGE_TYPE
import java.time.Instant

class TimingServerMessage : ServerMessage() {
    init {
        messageType = TIMING_CLIENT_MESSAGE_TYPE
        id = 0
    }

    val epochMilli = Instant.now().toEpochMilli()
}
