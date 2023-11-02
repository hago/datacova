/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.RegisterResponseMessage.Companion.MESSAGE_TYPE_REGISTER_RESPONSE

@WorkerMessage(type = MESSAGE_TYPE_REGISTER_RESPONSE)
class RegisterResponseMessage(val acknowledged: Boolean, val name: String) {

    companion object {
        const val MESSAGE_TYPE_REGISTER_RESPONSE: Byte = -1
    }
}