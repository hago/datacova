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
import com.hagoapp.datacova.entity.execution.ExecutionFileExtraCSV
import com.hagoapp.datacova.entity.execution.ExecutionFileInfo
import com.hagoapp.datacova.util.data.JDBCTypeUtils
import com.hagoapp.datacova.util.text.EncodingUtils
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.FileInputStream
import java.io.InputStream
import java.io.InvalidObjectException
import java.nio.charset.Charset
import java.sql.JDBCType

class CSVDataFileParser : DataFileParser {

    constructor(fileInfo: ExecutionFileInfo) {
        this.fileInfo = fileInfo
        //this.config =
    }

    private var fileInfo: ExecutionFileInfo? = null
    private var loaded = false
    private var format: CSVFormat? = null
    private val logger = CoVaLogger.getLogger()
    private var currentRow = 0
    private var guessedTypes: Map<Int, Pair<String, List<JDBCType>>>? = null
    private val data = mutableListOf<List<String>>()

    private var formats: List<CSVFormat> = listOf<CSVFormat>(
        CSVFormat.DEFAULT, CSVFormat.RFC4180, CSVFormat.EXCEL, CSVFormat.INFORMIX_UNLOAD, CSVFormat.INFORMIX_UNLOAD_CSV,
        CSVFormat.MYSQL, CSVFormat.ORACLE, CSVFormat.POSTGRESQL_CSV, CSVFormat.POSTGRESQL_TEXT, CSVFormat.TDF
    )
    private val formatNames: List<String> = listOf(
        "CSVFormat.DEFAULT",
        "CSVFormat.RFC4180",
        "CSVFormat.EXCEL",
        "CSVFormat.INFORMIX_UNLOAD",
        "CSVFormat.INFORMIX_UNLOAD_CSV",
        "CSVFormat.MYSQL",
        "CSVFormat.ORACLE",
        "CSVFormat.POSTGRESQL_CSV",
        "CSVFormat.POSTGRESQL_TEXT",
        "CSVFormat.TDF"
    )

    override fun close() {
        //
    }

    override fun guessColumnDefinitions(): Map<Int, Pair<String, List<JDBCType>>> {
        return guessedTypes!!
    }


    override fun hasMoreData(): Boolean {
        return currentRow < data.size
    }

    override fun nextLine(): List<Any?> {
        if (!hasMoreData()) {
            throw InvalidObjectException("No more line")
        }
        logger.debug("read line $currentRow")
        val line = data[currentRow]
        currentRow++
        return line
    }

    override fun open(fileInfo: ExecutionFileInfo) {
        this.prepare(fileInfo)
        val extra = fileInfo.extra as ExecutionFileExtraCSV
        val charset = Charset.forName(
            if (extra.encoding != null)
                EncodingUtils.normalizeEncoding(extra.encoding)
            else EncodingUtils.guessEncoding(fileInfo.name)
        )
        logger.debug("using $charset to parse file ${fileInfo.originalName}")
        for (i in formats.indices) {
            FileInputStream(fileInfo.name).use { fi ->
                try {
                    val fmt = formats[i]
                    parseCSV(fi, charset, fmt)
                    this.format = fmt
                    this.loaded = true
                    currentRow = 0
                    logger.debug("file ${fileInfo.name} parsed successfully with format ${formatNames[i]}, ${data.size} lines loaded")
                } catch (ex: Exception) {
                    logger.error(
                        "parsing file ${fileInfo.name} failed using format ${formatNames[i]} and encoding ${charset.displayName()}",
                        ex
                    )
                }
            }
            if (this.loaded) break
        }
        if (!this.loaded) {
            throw CoVaException("File parsing for ${fileInfo.name} failed")
        }
    }

    private fun prepare(fileInfo: ExecutionFileInfo) {
        val customizeCSVFormat = { fmt: CSVFormat ->
            var l = fmt.withFirstRecordAsHeader()
            val extra = fileInfo.extra as ExecutionFileExtraCSV
            //.withNullString("")
            if (extra.delimiter.isNotBlank()) {
                l = l.withDelimiter(extra.delimiter[0])
                //logger.debug("set limiter to ${extra.delimiter} for csv parser")
            }
            if (extra.quote.isNotBlank()) {
                l = l.withQuote((extra.quote[0]))
                //logger.debug("set quote to ${extra.quote} for csv parser")
            }
            l
        }
        this.formats = this.formats.map { fmt ->
            customizeCSVFormat(fmt)
        }
    }

    private fun parseCSV(ist: InputStream, charset: Charset, format: CSVFormat) {
        CSVParser.parse(ist, charset, format).use { parser ->
            val colCount = parser.headerMap.size
            val typeMap: MutableMap<Int, Pair<String, List<JDBCType>>> = mutableMapOf()
            parser.headerMap.forEach { name, i ->
                typeMap[i] = Pair(name, listOf())
            }
            parser.forEachIndexed { i, csvRecord ->
                if (csvRecord.size() != colCount) {
                    logger.error("element count ${csvRecord.size()} of row $i differs from header count $colCount")
                    throw Exception("format error found in ${fileInfo!!.name}")
                }
                csvRecord.forEachIndexed { j, cell ->
                    val possibleTypes = JDBCTypeUtils.guessTypes(cell.trim())
                    val existTypes = typeMap[j]!!.second
                    typeMap[j] = Pair(typeMap[j]!!.first, JDBCTypeUtils.combinePossibleTypes(existTypes, possibleTypes))
                }
                data.add(csvRecord.map { cell -> cell.trim() })
            }
            guessedTypes = typeMap
            //logger.debug("Data type detected as: $typeMap")
        }
    }
}
