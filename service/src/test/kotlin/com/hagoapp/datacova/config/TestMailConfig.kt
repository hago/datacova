/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config

data class TestMailConfig(
    val title: String,
    val body: String,
    val to: List<String>,
    val cc: List<String>,
    val bcc: List<String>
)
