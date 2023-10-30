/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.worker.execution.datafile

import com.hagoapp.datacova.CoVaException
import org.apache.poi.ss.format.CellFormatType
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import java.sql.JDBCType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class ExcelXDataFileWriter : DataFileWriter {

    private val wb = XSSFWorkbook()
    private val st = wb.createSheet()
    private val lines = mutableListOf<List<Any?>>()

    private var headerDef: List<String>? = null
    private var dataTypeDef: List<JDBCType>? = null

    override fun addData(data: List<Any?>) {
        if (headerDef == null) {
            throw CoVaException("headers not set!")
        }
        if (headerDef!!.size != data.size) {
            throw CoVaException("row size doesn't match headers size!")
        }
        lines.add(data)
    }

    override fun setDataType(types: List<JDBCType>) {
        if (headerDef == null) {
            throw CoVaException("headers not set!")
        }
        if (headerDef!!.size != types.size) {
            throw CoVaException("type list size doesn't match headers size!")
        }
        //println(types)
        dataTypeDef = types
    }

    override fun setHeader(headers: List<String>) {
        headerDef = headers
    }

    override fun write(outStream: OutputStream) {
        if ((headerDef == null) || (dataTypeDef == null)) {
            throw CoVaException("Header or type not set")
        }
        val headRow = st.createRow(0)
        headerDef!!.mapIndexed { index, header ->
            headRow.createCell(index, CellType.STRING).setCellValue(header)
        }
        val formatter = DateTimeFormatter.ISO_INSTANT
        lines.mapIndexed { index, line ->
            val row = st.createRow(index + 1)
            line.mapIndexed { cellIndex, item ->
                //println(item)
                val typePair = CellTypeUtils.jDBCTypeToCellType(dataTypeDef!![cellIndex])
                //println(typePair)
                val cell = row.createCell(cellIndex, typePair.first)
                when (typePair.first) {
                    CellType.NUMERIC -> cell.setCellValue(item.toString().toDouble())
                    CellType.BOOLEAN -> cell.setCellValue(item.toString().toBoolean())
                    CellType.STRING -> when (typePair.second) {
                        CellFormatType.TEXT -> cell.setCellValue(item.toString())
                        CellFormatType.DATE -> cell.setCellValue((item as ZonedDateTime).format(formatter))
                        else -> throw CoVaException("cell format type $typePair not supported")
                    }
                    else -> throw CoVaException("cell type $typePair not supported")
                }
            }
        }
        wb.write(outStream)
    }
}
