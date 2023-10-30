/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.worker.execution.datafile;

import java.io.OutputStream;
import java.sql.JDBCType;

interface DataFileWriter {
    fun setHeader(headers: List<String>)

    fun setDataType(types: List<JDBCType>)

    fun addData(data: List<Any?>)

    fun write(outStream: OutputStream)
}
