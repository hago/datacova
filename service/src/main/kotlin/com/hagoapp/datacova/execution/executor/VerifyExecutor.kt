/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.entity.action.verification.TaskActionVerify
import com.hagoapp.datacova.entity.execution.DataMessage
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.execution.executor.validator.ValidatorFactory
import com.hagoapp.f2t.ColumnDefinition
import com.hagoapp.f2t.DataTable
import com.hagoapp.f2t.ProgressNotify
import com.hagoapp.f2t.datafile.ParseResult

class VerifyExecutor : BaseTaskActionExecutor(), ProgressNotify {
    private lateinit var taskAction: TaskActionVerify
    private var verificationFailed = false
    private val logger = CoVaLogger.getLogger()

    override fun execute(taskExecution: TaskExecution, action: TaskAction, data: DataTable<out ColumnDefinition>) {
        if (action !is TaskActionVerify) {
            val ex = CoVaException("Not an TaskActionVerify: ${action.name}")
            watcher?.onError(action, ex)
            throw ex
        }
        taskAction = action
        val descriptions = mutableListOf<String>()
        val validators = action.configurations.map { conf ->
            descriptions.add(conf.describe(taskExecution.task.extra.locale))
            ValidatorFactory.createValidator(conf).withColumnDefinition(data.columnDefinition).setConfig(conf)
        }
        val size = data.rows.size.toFloat()
        data.rows.forEachIndexed { index, row ->
            validators.forEachIndexed { j, validator ->
                try {
                    validator.verify(row).forEach {
                        val message = createDataMessage(it.key, it.value, descriptions[j])
                        watcher?.onDataMessage(action, index, message)
                    }
                } catch (e: Exception) {
                    val message = createDataMessage(e.toString(), null, descriptions[j])
                    watcher?.onDataMessage(action, index, message)
                }
            }
            this.onProgress(index.toFloat() / size)
        }
        validators.forEach {
            try {
                it.close()
            } catch (e: Exception) {
                logger.error("closing validator of type {} causes error: {}", it.supportedVerificationType, e)
            }
        }
    }

    override fun mayContinueWhenDone(): Boolean {
        return !verificationFailed
    }

    private fun createDataMessage(fieldName: String, theValue: Any?, description: String): DataMessage {
        verificationFailed = true
        val message = DataMessage()
        with(message) {
            field = fieldName
            value = theValue
            descriptionExpected = description
        }
        return message
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
