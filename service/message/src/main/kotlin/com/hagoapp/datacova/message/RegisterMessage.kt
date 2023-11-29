/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.RegisterMessage.Companion.MESSAGE_TYPE_REGISTER

/**
 * Register message, from client to server for registration.
 *
 * @property group group the worker belongs
 * @property authKey authentication credential
 * @property name worker name, null for the first time registration
 * @property taskExecutionJob the task execution that the worker is working on, null if the worker is free
 * @property jobTime the time that the work load was issued, null if the worker is free
 * @constructor Create empty Register message
 */
@WorkerMessage(type = MESSAGE_TYPE_REGISTER)
class RegisterMessage(
    val group: String,
    val authKey: String,
    var name: String? = null,
    val taskExecutionJob: String? = null,
    val jobTime: Long? = null
) {

    companion object {
        const val MESSAGE_TYPE_REGISTER: Byte = 1
    }

    override fun toString(): String {
        return "RegisterMessage(group='$group', authKey='$authKey', name=$name, jobTime=$jobTime, job=$taskExecutionJob)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisterMessage

        if (group != other.group) return false
        if (authKey != other.authKey) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = group.hashCode()
        result = 31 * result + authKey.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

}
