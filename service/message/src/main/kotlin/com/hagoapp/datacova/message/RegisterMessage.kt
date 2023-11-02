/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.RegisterMessage.Companion.MESSAGE_TYPE_REGISTER

@WorkerMessage(type = MESSAGE_TYPE_REGISTER)
class RegisterMessage(val group: String) {

    companion object {
        const val MESSAGE_TYPE_REGISTER = 0x1
    }

    var name: String? = null
}
