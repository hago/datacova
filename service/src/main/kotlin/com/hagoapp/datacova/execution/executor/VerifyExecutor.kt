/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.entity.action.verification.TaskActionVerify
import com.hagoapp.datacova.execution.executor.validator.ValidatorFactory
import com.hagoapp.f2t.DataTable
import com.hagoapp.f2t.ProgressNotify
import com.hagoapp.f2t.datafile.ParseResult

class VerifyExecutor : BaseTaskActionExecutor(), ProgressNotify {
    private lateinit var taskAction: TaskActionVerify

    override fun execute(action: TaskAction, data: DataTable) {
        if (action !is TaskActionVerify) {
            val ex = CoVaException("Not an TaskActionVerify: ${action.name}")
            watcher?.onError(action, ex)
            throw ex
        }
        taskAction = action
        val validators = action.configurations.map { conf ->
            ValidatorFactory.createValidator(conf).withColumnDefinition(data.columnDefinition).withConfig(conf)
        }
        val size = data.rows.size.toFloat()
        data.rows.forEachIndexed { index, row ->
            validators.forEach { validator ->
                try {
                    validator.verify(row).forEach { f ->
                        val message = "Validation on field $f failed by rule ${validator.abstract}"
                        watcher?.onDataMessage(action, index, message)
                    }
                } catch (e: Exception) {
                    val message = ""
                    watcher?.onDataMessage(action, index, message)
                }
            }
            this.onProgress(index.toFloat() / size)
        }
    }

    override fun getActionType(): Int {
        return TaskActionVerify.TASK_ACTION_TYPE_VERIFY
    }

    override fun onStart() {
        watcher?.onProgressUpdate(taskAction, 0f)
    }

    override fun onComplete(p0: ParseResult?) {
        watcher?.onProgressUpdate(taskAction, 1f)
    }

    override fun onProgress(p0: Float) {
        watcher?.onProgressUpdate(taskAction, p0)
    }
}
