/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.HeartBeatResponseMessage.Companion.MESSAGE_TYPE_HEART_BEAT_RESPONSE

@WorkerMessage(type = MESSAGE_TYPE_HEART_BEAT_RESPONSE)
class HeartBeatResponseMessage(val timeStamp: Long, val originId: String) {
    companion object {
        const val MESSAGE_TYPE_HEART_BEAT_RESPONSE: Byte = -4
    }
}
