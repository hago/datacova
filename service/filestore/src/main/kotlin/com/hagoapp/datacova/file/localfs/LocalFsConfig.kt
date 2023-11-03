/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.file.localfs

import com.hagoapp.datacova.file.FsConfig
import com.hagoapp.datacova.file.FsScheme
import java.io.File
import java.io.InvalidObjectException

@FsScheme(name = LocalFsConfig.LOCAL_FS_SCHEME)
class LocalFsConfig() : FsConfig() {

    companion object {
        const val LOCAL_FS_SCHEME = "localFs"
    }

    constructor(root: String) : this() {
        rootPath = root
    }

    var rootPath: String = File("./").absolutePath
    override fun serialize(): String {
        return rootPath
    }

    override fun loadConnectionString(input: String) {
        if (!input.startsWith("$LOCAL_FS_SCHEME:", true)) {
            throw InvalidObjectException("Not a valid LocalFsConfig connection string: $input")
        }
        rootPath = input.substring(LOCAL_FS_SCHEME.length+1).trim()
    }
}
