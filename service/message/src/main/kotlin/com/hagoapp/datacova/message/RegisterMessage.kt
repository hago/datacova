/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.RegisterMessage.Companion.MESSAGE_TYPE_REGISTER

@WorkerMessage(type = MESSAGE_TYPE_REGISTER)
class RegisterMessage(val group: String, var name: String? = null) {

    companion object {
        const val MESSAGE_TYPE_REGISTER: Byte = 1
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisterMessage

        if (group != other.group) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "RegisterMessage(group='$group', name=$name)"
    }
}
