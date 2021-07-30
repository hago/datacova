/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.entity.action.distribute.Configuration
import com.hagoapp.datacova.entity.action.distribute.TaskActionDistribute
import com.hagoapp.datacova.entity.action.distribute.TaskActionDistribute.TASK_ACTION_TYPE_DISTRIBUTE
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.execution.datafile.CSVDataFileWriter
import com.hagoapp.datacova.execution.datafile.ExcelDataFileWriter
import com.hagoapp.datacova.execution.datafile.ExcelXDataFileWriter
import com.hagoapp.datacova.execution.distribute.DistributorFactory
import com.hagoapp.datacova.util.FileStoreUtils
import com.hagoapp.datacova.util.Utils
import com.hagoapp.f2t.DataTable
import com.hagoapp.f2t.datafile.FileInfo
import com.hagoapp.f2t.datafile.csv.FileInfoCsv
import com.hagoapp.f2t.datafile.excel.FileInfoExcel
import com.hagoapp.f2t.datafile.excel.FileInfoExcelX
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DistributeExecutor : BaseTaskActionExecutor() {

    private lateinit var execution: TaskExecution

    override fun execute(taskExecution: TaskExecution, action: TaskAction, data: DataTable) {
        if (action !is TaskActionDistribute) {
            throw CoVaException("Invalid distribute action")
        }
        this.execution = taskExecution
        val filename = prepareSourceFile(taskExecution.id, action.configuration, data, taskExecution.fileInfo.fileInfo)
        action.configuration.targetFileName = prepareTargetFile(action)
        //println(action.configuration.toJson())
        DistributorFactory.getDistributor(action).distribute(filename)
    }

    private fun prepareSourceFile(id: Int, conf: Configuration, data: DataTable, fileInfo: FileInfo): String {
        return if (conf.isCopyOriginal) fileInfo.filename!!
        else {
            val d = DateTimeFormatter.ofPattern("yyyy/MM/dd/yyyyMMddHHmmssSSS").withZone(ZoneId.of("UTC"))
            val writer = when (fileInfo) {
                is FileInfoCsv -> {
                    val csvWriter = CSVDataFileWriter()
                    csvWriter.setHeader(data.columnDefinition.map { it.name })
                    csvWriter
                }
                is FileInfoExcel -> {
                    val xlsWriter = ExcelDataFileWriter()
                    xlsWriter.setHeader(data.columnDefinition.map { it.name })
                    xlsWriter.setDataType(data.columnDefinition.map { it.inferredType!! })
                    xlsWriter
                }
                is FileInfoExcelX -> {
                    val xlsxWriter = ExcelXDataFileWriter()
                    xlsxWriter.setHeader(data.columnDefinition.map { it.name })
                    xlsxWriter.setDataType(data.columnDefinition.map { it.inferredType!! })
                    xlsxWriter
                }
                else -> throw CoVaException(String.format("unsupported data file type: %s", fileInfo))
            }
            val s = Utils.parseFileName(execution.fileInfo.originalName).ext
            val p = "execution/${id}/${d.format(Instant.now())}${if (s.isBlank()) "" else ".$s"}"
            for (i in 0 until data.rows.size) {
                writer.addData(data.rows[i].cells.map { it.data })
            }
            val fs = FileStoreUtils.getTemporaryFileStore()
            ByteArrayOutputStream().use {
                writer.write(it)
                return fs.saveFileToStore(p, ByteArrayInputStream(it.toByteArray()))
            }
        }
    }

    private fun prepareTargetFile(action: TaskActionDistribute): String {
        return action.configuration.targetFileName ?: execution.fileInfo.originalName
    }

    override fun getActionType(): Int {
        return TASK_ACTION_TYPE_DISTRIBUTE;
    }
}
