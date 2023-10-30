/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.worker.execution.datafile

import com.hagoapp.datacova.CoVaException
import java.io.OutputStream
import java.sql.JDBCType

class CSVDataFileWriter : DataFileWriter {

    private val content = mutableListOf<List<Any?>>()
    private var headers: List<String>? = null

    override fun setHeader(headers: List<String>) {
        this.headers = headers
    }

    override fun setDataType(types: List<JDBCType>) {
        // do nothing
    }

    override fun addData(data: List<Any?>) {
        if (headers == null) {
            throw CoVaException("headers not set!")
        }
        if (headers!!.size != data.size) {
            throw CoVaException("incorrect column size")
        }
        content.add(data)
    }

    override fun write(outStream: OutputStream) {
        val lf = "\r\n".toByteArray()
        outStream.write(headers!!.joinToString().toByteArray())
        outStream.write(lf)
        content.map { line ->
            outStream.write(line.joinToString {
                when (it) {
                    null -> ""
                    else -> "\"${it.toString().replace("\"","\"\"")}\""
                }
            }.toByteArray())
            outStream.write(lf)
        }
    }
}