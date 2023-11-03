/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket.execution

import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.web.websocket.ServerMessage

data class CompleteMessage(
    val execution: TaskExecution
) : ServerMessage() {
    init {
        messageType = 1002
    }
}
