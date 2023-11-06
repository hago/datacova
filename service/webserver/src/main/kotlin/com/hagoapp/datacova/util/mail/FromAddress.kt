/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.mail

import com.hagoapp.datacova.utility.CoVaException
import java.nio.charset.StandardCharsets
import javax.mail.internet.InternetAddress

class FromAddress private constructor(input: String){
    companion object {
        fun parse(input: String): FromAddress {
            return FromAddress(input)
        }
    }

    private val name: String
    private val email: String
    init {
        val parts = input.trim().split("<")
        when {
            parts.size == 1 -> {
                name = ""
                email = input
            }
            parts.size > 2 -> throw CoVaException("Invalid from address $input")
            !parts[1].endsWith('>') -> throw CoVaException("Invalid from address $input")
            else -> {
                name = parts[0]
                email = parts[1].substringBefore(">")
            }
        }
    }

    fun toInternetAddress(): InternetAddress {
        return InternetAddress(email, name, StandardCharsets.UTF_8.name())
    }

    fun toEmail(): String {
        return email
    }

    fun getName(): String {
        return name
    }
}
