/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.HeartBeatMessage.Companion.MESSAGE_TYPE_HEART_BEAT

/**
 * Heart beat message from server to client.
 *
 * @property timeStamp the timestamp when the message is sent
 * @property id the identifier of this message
 * @constructor Create empty Heart beat message
 */
@WorkerMessage(type = MESSAGE_TYPE_HEART_BEAT)
class HeartBeatMessage(val timeStamp: Long, val id: String) {
    companion object {
        const val MESSAGE_TYPE_HEART_BEAT: Byte = 4
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeartBeatMessage

        if (timeStamp != other.timeStamp) return false
        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeStamp.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }
}
