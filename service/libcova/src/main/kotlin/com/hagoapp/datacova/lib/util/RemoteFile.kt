/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util

class RemoteFile(
    val name: String,
    val isDirectory: Boolean,
    val isSymbolLink: Boolean,
    val size: Long,
    val base: String
) {
    override fun toString(): String {
        return "RemoteFile(name='$name', isDirectory=$isDirectory, isSymbolLink=$isSymbolLink, size=$size, base='$base')"
    }
}
