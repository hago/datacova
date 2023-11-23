/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.executor

import com.hagoapp.datacova.utility.CoVaException
import com.hagoapp.datacova.lib.action.TaskAction
import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.lib.ingest.TaskActionIngest
import com.hagoapp.datacova.lib.ingest.TaskActionIngest.TASK_ACTION_TYPE_INGEST
import com.hagoapp.datacova.worker.Application
import com.hagoapp.datacova.worker.execution.DbConfigLoader
import com.hagoapp.f2t.*
import com.hagoapp.f2t.datafile.ParseResult

class IngestExecutor : BaseTaskActionExecutor(), ProgressNotify {
    private lateinit var taskAction: TaskActionIngest
    private lateinit var result: ParseResult
    private val config = Application.oneApp().config

    override fun execute(taskExecution: TaskExecution, action: TaskAction, data: DataTable<out ColumnDefinition>) {
        if (action !is TaskActionIngest) {
            throw CoVaException()
        }
        taskAction = action
        val dbConfig = DbConfigLoader.provider.lookup(action.connectionId) ?: throw CoVaException()
        val fileTable = DataTable(data.columnDefinition.mapIndexed { i, col ->
            val fileCol = FileColumnDefinition(col.name, setOf(col.dataType), col.dataType)
            fileCol.order = i
            fileCol.typeModifier = col.typeModifier
            fileCol
        }, data.rows)
        val d2t = D2TProcess(fileTable, dbConfig.createConnection(), action.ingestOptions)
        d2t.progressNotifier = this
        result = d2t.run()
    }

    override fun getActionType(): Int {
        return TASK_ACTION_TYPE_INGEST
    }

    override fun onStart() {
        watcher?.onProgressUpdate(taskAction, 0f)
    }

    override fun onComplete(p0: ParseResult) {
        if (p0.isSucceeded) {
            watcher?.onProgressUpdate(taskAction, 1f)
        } else {
            watcher?.onError(taskAction, Exception(p0.getErrors().values.first()))
        }
    }

    override fun onProgress(p0: Float) {
        watcher?.onProgressUpdate(taskAction, p0)
    }

}
