/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.WorkerDoneMessage.Companion.MESSAGE_TYPE_MESSAGE_DONE

/**
 * The message sent from client to server to inform that a task execution is done, successfully or not.
 *
 * @author suncjs
 * @since 0.5
 */
@WorkerMessage(type = MESSAGE_TYPE_MESSAGE_DONE)
class WorkerDoneMessage(val taskExecutionJson: String, val resultJson: String) {
    companion object {
        const val MESSAGE_TYPE_MESSAGE_DONE: Byte = 3
    }
}