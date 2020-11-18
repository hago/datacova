/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.datafile

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.entity.execution.ExecutionFileInfo
import com.hagoapp.datacova.util.data.JDBCTypeUtils
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.*
import java.io.FileInputStream
import java.io.InvalidObjectException
import java.sql.JDBCType
import java.time.ZoneId
import java.time.ZonedDateTime

class ExcelDataFileParser : DataFileParser {

    private var wb: Workbook? = null
    private val cols: MutableMap<Int, Pair<String, List<JDBCType>>> = mutableMapOf()
    private val data: MutableList<List<Any?>> = mutableListOf()
    private var index = 0
    private val logger = CoVaLogger.getLogger()

    override fun close() {
        wb?.close()
    }

    override fun guessColumnDefinitions(): Map<Int, Pair<String, List<JDBCType>>> {
        return cols
    }

    override fun hasMoreData(): Boolean {
        return index < data.size
    }

    override fun nextLine(): List<Any?> {
        if (!hasMoreData()) {
            throw InvalidObjectException("No more line")
        }
        logger.debug("read line $index")
        val ret = data[index]
        index++
        return ret
    }

    override fun open(fileInfo: ExecutionFileInfo) {
        WorkbookFactory.create(FileInputStream(fileInfo.name)).use { wb ->
            val st = wb!!.getSheetAt(0)
            for (i in st.firstRowNum..st.lastRowNum) {
                val row = st.getRow(i)
                when (i) {
                    st.firstRowNum -> row.forEachIndexed { colIndex, cell ->
                        cols[colIndex] = Pair(cell.stringCellValue, mutableListOf())
                    }
                    else -> {
                        val l = mutableListOf<Any?>()
                        if (row.lastCellNum > cols.size) {
                            throw CoVaException("format error in ${fileInfo.originalName}, line $i contains more cells than field row")
                        }
                        for (colIndex in 0 until row.lastCellNum) {
                            val cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                            val possibleTypes = guessCellType(cell)
                            val existingTypes = cols.getValue(colIndex).second
                            cols[colIndex] =
                                Pair(
                                    cols.getValue(colIndex).first,
                                    JDBCTypeUtils.combinePossibleTypes(existingTypes, possibleTypes)
                                )
                            l.add(cell)
                        }
                        data.add(l)
                    }
                }
            }
            cols.replaceAll { _, def ->
                Pair(def.first, listOf(JDBCTypeUtils.guessMostAccurateType(def.second)))
            }
            data.replaceAll { row ->
                row.mapIndexed { index, cell ->
                    getCellValue(cell as Cell, cols.getValue(index).second[0])
                }
            }
        }
    }

    private fun guessCellType(cell: Cell): List<JDBCType> {
        return when {
            cell.cellType == CellType.BOOLEAN -> listOf(JDBCType.BOOLEAN)
            (cell.cellType == CellType.NUMERIC) && DateUtil.isCellDateFormatted(cell) -> listOf(JDBCType.TIMESTAMP)
            cell.cellType == CellType.NUMERIC -> {
                val nv = cell.numericCellValue
                if (nv == nv.toLong().toDouble()) listOf(JDBCType.DOUBLE, JDBCType.BIGINT) else listOf(
                    JDBCType.DOUBLE
                )
            }
            cell.cellType == CellType.BLANK -> listOf()
            else -> JDBCTypeUtils.guessTypes(cell.stringCellValue)
        }
    }

    private fun getCellValue(cell: Cell, type: JDBCType): Any? {
        return when {
            cell.cellType == CellType.BOOLEAN -> cell.booleanCellValue
            (cell.cellType == CellType.NUMERIC) && DateUtil.isCellDateFormatted(cell) -> ZonedDateTime.ofInstant(
                cell.dateCellValue.toInstant(),
                ZoneId.systemDefault()
            )
            cell.cellType == CellType.NUMERIC -> if (type == JDBCType.BIGINT) cell.numericCellValue.toLong() else cell.numericCellValue
            cell.cellType == CellType.BLANK -> cell.stringCellValue
            else -> JDBCTypeUtils.toTypedValue(cell.stringCellValue, type)
        }
    }
}
