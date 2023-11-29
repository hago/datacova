/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.RegisterResponseMessage.Companion.MESSAGE_TYPE_REGISTER_RESPONSE

/**
 * Register response message from server to client
 *
 * @property acknowledged true for successful registration, otherwise false
 * @property name the unique worker name assigned by server
 * @constructor Create empty Register response message
 */
@WorkerMessage(type = MESSAGE_TYPE_REGISTER_RESPONSE)
class RegisterResponseMessage(val acknowledged: Boolean, val name: String) {

    companion object {
        const val MESSAGE_TYPE_REGISTER_RESPONSE: Byte = -1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisterResponseMessage

        if (acknowledged != other.acknowledged) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = acknowledged.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String {
        return "RegisterResponseMessage(acknowledged=$acknowledged, name='$name')"
    }
}
