/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution

import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.web.websocket.WebSocketManager
import com.hagoapp.datacova.web.websocket.execution.CompleteMessage
import com.hagoapp.datacova.web.websocket.execution.StartMessage

class WebSocketNotifier {
    companion object {
        fun notifyStart(te: TaskExecution) {
            val user = UserCache.getUser(te.addBy)
            val message = StartMessage(te)
            WebSocketManager.getManager().sendUserMessage(message, user)
        }

        fun notifyComplete(te: TaskExecution) {
            val user = UserCache.getUser(te.addBy)
            val message = CompleteMessage(te)
            WebSocketManager.getManager().sendUserMessage(message, user)
        }
    }
}
