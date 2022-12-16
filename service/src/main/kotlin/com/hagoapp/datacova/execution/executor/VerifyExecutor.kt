/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.verification.TaskActionVerify
import com.hagoapp.datacova.entity.execution.DataMessage
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.util.surveyor.RuleConfigDescriptor
import com.hagoapp.f2t.ColumnDefinition
import com.hagoapp.f2t.DataTable
import com.hagoapp.f2t.ProgressNotify
import com.hagoapp.f2t.datafile.ParseResult
import com.hagoapp.surveyor.SurveyorFactory
import org.slf4j.LoggerFactory

class VerifyExecutor : BaseTaskActionExecutor(), ProgressNotify {
    private lateinit var taskAction: TaskActionVerify
    private var verificationFailed = false
    private val logger = LoggerFactory.getLogger(VerifyExecutor::class.java)

    override fun execute(taskExecution: TaskExecution, action: TaskAction, data: DataTable<out ColumnDefinition>) {
        if (action !is TaskActionVerify) {
            val ex = CoVaException("Not an TaskActionVerify: ${action.name}")
            watcher?.onError(action, ex)
            throw ex
        }
        taskAction = action
        val validators = action.configurations.map { conf ->
            // descriptions.add(conf.describe(taskExecution.task.extra.locale))
            // ValidatorFactory.createValidator(conf).withColumnDefinition(data.columnDefinition).setConfig(conf)
            val validator = SurveyorFactory.createSurveyor(conf.ruleConfig)
            val columnIndexes = conf.fields.map { f ->
                data.columnDefinition.indexOfFirst { it.name == f }
            }
            Pair(validator, columnIndexes)
        }
        val descriptions = action.configurations.map { conf ->
            RuleConfigDescriptor.create(conf.ruleConfig).describe(conf, taskExecution.task.extra.locale)
        }
        data.rows.forEachIndexed { i, row ->
            validators.forEachIndexed { j, validator ->
                try {
                    val r = validator.first.process(validator.second.map { row.cells[it] })
                    logger.trace("""validate $row against validator "${action.configurations[j].ruleConfig}", result: $r""")
                    if (!r) {
                        verificationFailed = true
                        validator.second.map { colIndex ->
                            val col = data.columnDefinition[colIndex].name
                            val v = row.cells[colIndex].data
                            watcher?.onDataMessage(action, i, createDataMessage(col, v, descriptions[j]))
                        }
                    }
                } catch (e: Exception) {
                    val message = createDataMessage(e.toString(), null, descriptions[j])
                    watcher?.onDataMessage(action, i, message)
                }
            }
        }
        validators.forEachIndexed { i, it ->
            try {
                it.first.close()
            } catch (e: Exception) {
                logger.error(
                    "closing validator of type {} causes error: {}",
                    action.configurations[i].ruleConfig.configType, e
                )
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
