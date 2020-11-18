/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.datafile

import com.hagoapp.datacova.entity.execution.ExecutionFileInfo
import java.io.Closeable
import java.sql.JDBCType

interface DataFileParser : Closeable {
    fun open(fileInfo: ExecutionFileInfo)
    fun guessColumnDefinitions(): Map<Int, Pair<String, List<JDBCType>>>
    fun nextLine(): List<Any?>
    fun hasMoreData(): Boolean
}