/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.datafile

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.entity.execution.ExecutionFileExtraCSV
import com.hagoapp.datacova.entity.execution.ExecutionFileExtraExcel
import com.hagoapp.datacova.entity.execution.ExecutionFileExtraExcelOpenXML
import com.hagoapp.datacova.entity.execution.ExecutionFileInfo
import com.hagoapp.datacova.util.Utils
import com.hagoapp.datacova.util.data.JDBCTypeUtils
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.sql.JDBCType

class DataContainer {
    private constructor() {
        //
    }

    private var fileInfo: ExecutionFileInfo? = null
    val sourceFileInfo: ExecutionFileInfo?
        get() = fileInfo
    private var columnDef: Map<Int, Pair<String, List<JDBCType>>> = mapOf()
    val columnDefinition
        get() = columnDef
    private val data: MutableList<List<Any?>> = mutableListOf()
    val size: Int
        get() = data.size

    private var outColDef: Map<String, JDBCType>? = null
    var outputColumnDefinition
        get() = outColDef
        set(value) {
            outColDef = value
        }

    private fun getRow(i: Int): List<Any?> {
        if (i >= data.size) {
            throw IndexOutOfBoundsException("index $i exceeds size ${data.size} of DataContainer object")
        }
        return data[i]
    }

    fun getNamedRow(i: Int): Map<String, Any?> {
        return if (outColDef != null) getNamedRow(i, outColDef!!)
        else getNamedRow(i, columnDefinition.map { entry ->
            Pair(entry.value.first, JDBCType.CLOB)
        }.toMap())
    }

    fun getNamedRow(i: Int, colDef: Map<String, JDBCType>): Map<String, Any?> {
        return getRow(i).mapIndexed { index, item ->
            val col = columnDef.getValue(index).first
            Pair(col, JDBCTypeUtils.toTypedValue(item, colDef.getValue(col)))
        }.toMap()
    }

    fun getUnTypedNamedRow(i: Int): Map<String, Any?> {
        return getRow(i).mapIndexed { index, item ->
            val col = columnDef.getValue(index).first
            Pair(col, item)
        }.toMap()
    }

    companion object {
        fun getDataContainer(fileInfo: ExecutionFileInfo): DataContainer {
            val config = CoVaConfig.getConfig()
            val dc = DataContainer()
            dc.fileInfo = fileInfo
            dc.fileInfo!!.name = Utils.joinPath(config.task.fileStoreRoot, fileInfo.name)
            getParser(dc.fileInfo!!).use { parser ->
                parser.open(dc.fileInfo!!)
                dc.columnDef = parser.guessColumnDefinitions()
                while (parser.hasMoreData()) {
                    val line = parser.nextLine()
                    dc.data.add(line)
                }
            }
            return dc
        }

        private fun getParser(fileInfo: ExecutionFileInfo): DataFileParser {
            return when (fileInfo.extra) {
                is ExecutionFileExtraCSV -> CSVDataFileParser(fileInfo)
                is ExecutionFileExtraExcel, is ExecutionFileExtraExcelOpenXML -> ExcelDataFileParser()
                else -> {
                    val d = fileInfo.originalName.lastIndexOf(".")
                    if (d <= 0) {
                        throw NotImplementedError("Type of file ${fileInfo.originalName} can't be determined")
                    }
                    val ext = fileInfo.originalName.substring(d).toLowerCase()
                    return when (ext) {
                        ".csv" -> CSVDataFileParser(fileInfo)
                        ".xls", ".xlsx" -> ExcelDataFileParser()
                        else -> throw NotImplementedError("File type $ext is not supported")
                    }
                }
            }
        }

        fun toByteArray(
            dataContainer: DataContainer,
            outputFileType: String = "csv", @Suppress("UNUSED_PARAMETER") vararg extra: Any
        ): ByteArray {
            ByteArrayOutputStream().use { out ->
                writeDataStream(dataContainer, out, outputFileType, extra)
                return out.toByteArray()
            }
        }

        fun writeDataStream(
            dataContainer: DataContainer,
            output: OutputStream,
            outputFileType: String? = null,
            @Suppress("UNUSED_PARAMETER") vararg extra: Any
        ) {
            val d = dataContainer.sourceFileInfo!!.originalName.lastIndexOf(".")
            val ext = dataContainer.sourceFileInfo!!.originalName.substring(d).toLowerCase()
            val writer = when (val type =
                if ((outputFileType == null) || listOf(".csv", ".xls", ".xlsx").contains(ext)) ext else "csv") {
                ".csv" -> CSVDataFileWriter()
                ".xls" -> ExcelDataFileWriter()
                ".xlsx" -> ExcelXDataFileWriter()
                else -> throw NotImplementedError("File type $type is not supported")
            }
            writer.setHeader(dataContainer.columnDefinition.values.map { it.first })
            writer.setDataType(dataContainer.columnDefinition.values.map { it.second[0] })
            dataContainer.data.forEach { line ->
                writer.addData(line)
            }
            writer.write(output)
        }
    }
}
