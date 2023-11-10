/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.file.memory

import com.hagoapp.datacova.file.FsConfig
import com.hagoapp.datacova.file.FsScheme
import com.hagoapp.datacova.file.memory.MemFsConfig.Companion.MEMORY_FS_SCHEME

@FsScheme(name = MEMORY_FS_SCHEME)
class MemFsConfig: FsConfig() {

    companion object {
        const val MEMORY_FS_SCHEME = "memFs"
    }
    override fun serialize(): String {
        return MEMORY_FS_SCHEME
    }

    override fun loadConnectionString(input: String) {
        //
    }
}
