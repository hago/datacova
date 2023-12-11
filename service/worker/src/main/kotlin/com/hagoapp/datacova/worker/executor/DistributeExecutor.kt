/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.executor

import com.hagoapp.datacova.file.FileStoreFactory
import com.hagoapp.datacova.worker.executor.distribute.DistributorFactory
import com.hagoapp.datacova.utility.CoVaException
import com.hagoapp.datacova.utility.Utils
import com.hagoapp.datacova.lib.util.datafile.CSVDataFileWriter
import com.hagoapp.datacova.lib.util.datafile.ExcelDataFileWriter
import com.hagoapp.datacova.lib.util.datafile.ExcelXDataFileWriter
import com.hagoapp.datacova.lib.action.TaskAction
import com.hagoapp.datacova.lib.distribute.Configuration
import com.hagoapp.datacova.lib.distribute.TaskActionDistribute
import com.hagoapp.datacova.lib.distribute.TaskActionDistribute.TASK_ACTION_TYPE_DISTRIBUTE
import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.worker.Application
import com.hagoapp.f2t.ColumnDefinition
import com.hagoapp.f2t.DataTable
import com.hagoapp.f2t.datafile.csv.FileInfoCsv
import com.hagoapp.f2t.datafile.excel.FileInfoExcel
import com.hagoapp.f2t.datafile.excel.FileInfoExcelX
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DistributeExecutor : BaseTaskActionExecutor() {

    private lateinit var execution: TaskExecution
    private lateinit var conf: Configuration
    private val fsStoreConnection = Application.oneApp().config.fileStore

    override fun execute(taskExecution: TaskExecution, action: TaskAction, data: DataTable<out ColumnDefinition>) {
        if (action !is TaskActionDistribute) {
            throw CoVaException("Invalid distribute action")
        }
        this.execution = taskExecution
        val originalName = execution.fileInfo.originalName
        conf = action.configuration
        val targetFileName = conf.targetFileName ?: originalName
        if (conf.isCopyOriginal) {
            FileStoreFactory.createFileStore(fsStoreConnection).getFile(execution.fileInfo.fileId)
        } else {
            createDataFile(data)
        }.use {
            action.configuration.targetFileName = targetFileName
            DistributorFactory.getDistributor(action).distribute(it)
        }
    }

    private fun createDataFile(
        data: DataTable<out ColumnDefinition>
    ): InputStream {
        val writer = when (execution.fileInfo.fileInfo) {
            is FileInfoCsv -> {
                val csvWriter = CSVDataFileWriter()
                csvWriter.setHeader(data.columnDefinition.map { it.name })
                csvWriter
            }

            is FileInfoExcelX -> {
                val xlsxWriter = ExcelXDataFileWriter()
                xlsxWriter.setHeader(data.columnDefinition.map { it.name })
                xlsxWriter.setDataType(data.columnDefinition.map { it.dataType!! })
                xlsxWriter
            }

            is FileInfoExcel -> {
                val xlsWriter = ExcelDataFileWriter()
                xlsWriter.setHeader(data.columnDefinition.map { it.name })
                xlsWriter.setDataType(data.columnDefinition.map { it.dataType!! })
                xlsWriter
            }

            else -> throw CoVaException(String.format("unsupported data file type: %s", execution.fileInfo.fileInfo))
        }
        val extName = Utils.parseFileName(execution.fileInfo.originalName).ext
        val d = DateTimeFormatter.ofPattern("yyyy/MM/dd/yyyyMMddHHmmssSSS").withZone(ZoneId.of("UTC"))
        val tempFileName = File(
            Utils.getSystemTemporaryDirectory(),
            "execution/${execution.id}/${d.format(Instant.now())}${if (extName.isBlank()) "" else ".$extName"}"
        )
        for (i in 0 until data.rows.size) {
            writer.addData(data.rows[i].cells.map { it.data })
        }
        FileOutputStream(tempFileName).use { writer.write(it) }
        return FileInputStream(tempFileName)
    }

    override fun getActionType(): Int {
        return TASK_ACTION_TYPE_DISTRIBUTE
    }
}
