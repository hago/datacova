/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.file

import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.jvm.Throws

interface FileStore {
    fun putFile(src: InputStream, fileName: String, size: Long): String
    fun getFile(id: String): InputStream
    fun getFileInfo(id: String): StoreFileInfo

    @Throws(FileNotFoundException::class, AccessDeniedException::class)
    fun delete(id: String): Boolean

    fun exists(id: String): Boolean
}
