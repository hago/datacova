/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.HeartBeatResponseMessage.Companion.MESSAGE_TYPE_HEART_BEAT_RESPONSE

/**
 * Heart beat response message from client to server.
 *
 * @property timeStamp the timestamp when the heartbeat is responded
 * @property originId the identity of heartbeat
 * @constructor Create empty Heart beat response message
 */
@WorkerMessage(type = MESSAGE_TYPE_HEART_BEAT_RESPONSE)
class HeartBeatResponseMessage(val timeStamp: Long, val originId: String) {
    companion object {
        const val MESSAGE_TYPE_HEART_BEAT_RESPONSE: Byte = -4
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeartBeatResponseMessage

        if (timeStamp != other.timeStamp) return false
        if (originId != other.originId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeStamp.hashCode()
        result = 31 * result + originId.hashCode()
        return result
    }
}
