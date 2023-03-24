/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.file.localfs

import com.hagoapp.datacova.file.FileInfo

class LocalFsFileInfo(
    override var originalFileName: String? = null,
    override var id: String? = null,
    override var size: Long? = null,
    var path: String? = null
) : FileInfo(originalFileName, id, size)