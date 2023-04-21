/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.file

/**
 * Meta information for a file stored in store.
 */
open class StoreFileInfo(
    open var originalFileName: String? = null,
    open var id: String? = null,
    open var size: Long? = null
)
