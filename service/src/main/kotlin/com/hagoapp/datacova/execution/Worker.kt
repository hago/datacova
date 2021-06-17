/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.data.execution.TaskExecutionData
import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.entity.execution.DataMessage
import com.hagoapp.datacova.entity.execution.ExecutionActionDetail
import com.hagoapp.datacova.entity.execution.ExecutionDetail
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.execution.executor.report.Reporter
import com.hagoapp.datacova.executor.Executor
import com.hagoapp.datacova.util.FileStoreUtils
import com.hagoapp.datacova.util.StackTraceWriter
import com.hagoapp.f2t.DataTable
import com.hagoapp.f2t.FileParser

class Worker(taskExecution: TaskExecution) : TaskExecutionActionWatcher, TaskExecutionWatcher {

    private val logger = CoVaLogger.getLogger()
    private val taskExec = taskExecution
    private val observers = mutableListOf<TaskExecutionWatcher>()
    private val detail = ExecutionDetail(taskExecution)
    private var currentActionDetail: ExecutionActionDetail? = null
    private var currentActionIndex: Int = 0

    fun addWatcher(watcher: TaskExecutionWatcher): Worker {
        observers.add(watcher)
        return this
    }

    init {
        Executor.getExecutor().workerStarts(taskExecution)
        addWatcher(this)
    }

    fun execute() {
        detail.startTiming()
        observers.forEach { it.onStart(taskExec) }
        val dt: DataTable
        try {
            observers.forEach { it.onDataLoadStart(taskExec) }
            taskExec.fileInfo.fileInfo.filename =
                FileStoreUtils.getUploadedFileStore().getFullFileName(taskExec.fileInfo.fileInfo.filename!!)
            val parser = FileParser(taskExec.fileInfo.fileInfo)
            dt = parser.extractData()
            observers.forEach { it.onDataLoadComplete(taskExec, true) }
            detail.lineCount = dt.rows.size
        } catch (ex: Exception) {
            val msg =
                "Data source loading fail, abort execution ${taskExec.id} of task ${taskExec.task.name}(${taskExec.taskId})"
            logger.error(msg)
            observers.forEach { it.onError(taskExec, ex) }
            observers.forEach { it.onDataLoadComplete(taskExec, false) }
            detail.dataLoadingError = ex
            detail.endTiming()
            observers.forEach { it.onComplete(taskExec, detail) }
            return
        }
        for (i in 0 until taskExec.task.actions.size) {
            currentActionIndex = i
            val action = taskExec.task.actions[i]
            currentActionDetail = detail.addActionDetail(i, action)
            observers.forEach { it.onActionStart(taskExec, i) }
            try {
                val executor = TaskActionExecutorFactory.createTaskActionExecutor(action)
                executor.locale = taskExec.task.extra.locale
                executor.watcher = this
                executor.execute(taskExec.task, action, dt)
                currentActionDetail!!.end()
                observers.forEach { it.onActionComplete(taskExec, i, currentActionDetail!!) }
                if ((i < taskExec.task.actions.size - 1) && !executor.mayContinueWhenDone()) {
                    logger.info("action $i: ${action.name} completed, and it prevents following actions to proceed")
                    break
                }
            } catch (ex: Exception) {
                logger.error("Error occurs in action $i: ${action.name} of execution ${taskExec.id}: ${ex.message}")
                StackTraceWriter.write(ex, logger)
                currentActionDetail!!.error = ex;
                currentActionDetail!!.end();
                observers.forEach { it.onActionComplete(taskExec, i, currentActionDetail!!) }
                if (action.extra.continueNextWhenError) {
                    logger.info("continue next action of execution ${taskExec.id}")
                    continue
                } else {
                    logger.info("abort following actions of execution ${taskExec.id}")
                    break
                }
            }
        }
        detail.endTiming()
        observers.forEach { it.onComplete(taskExec, detail) }
    }

    /**
     * begin of TaskExecutionActionWatcher
     **/
    override fun onError(action: TaskAction, error: Exception): Boolean {
        currentActionDetail!!.error = error
        return false
    }

    override fun onMessage(action: TaskAction, msg: String) {
        // currentActionDetail!!.
    }

    override fun onDataMessage(action: TaskAction, lineNo: Int, msg: DataMessage) {
        currentActionDetail!!.dataMessages.compute(lineNo) { _, existed ->
            existed?.plus(msg) ?: mutableListOf(msg)
        }
    }

    override fun onProgressUpdate(action: TaskAction, progress: Float) {
        logger.info("${"%.2f".format(progress * 100)}% of action ${action.name} processed")
    }

    /**
     * begin of TaskExecutionWatcher
     **/
    override fun onStart(te: TaskExecution) {
        try {
            WebSocketNotifier.notifyStart(te)
            TaskExecutionData().startTaskExecution(te)
            logger.info("Execution ${te.id} of Task ${te.task.name}(${te.taskId}) started")
        } catch (ex: Exception) {
            logger.error("unexpected error in Execution Service call onStart back: $ex")
            StackTraceWriter.write(ex, logger)
        }
    }

    override fun onComplete(te: TaskExecution, result: ExecutionDetail) {
        try {
            WebSocketNotifier.notifyComplete(te)
            TaskExecutionData().completeTaskExecution(result)
            Reporter.sendReport(te, result)
            logger.info("Execution ${te.id} of Task ${te.task.name}(${te.taskId}) ${if (result.isSucceeded) "succeeded" else "failed"}")
        } catch (ex: Throwable) {
            logger.error("unexpected error in Execution Service call onComplete back: $ex")
            StackTraceWriter.write(ex, logger)
        }
        Executor.getExecutor().workerCompletes(te)
    }

    override fun onError(te: TaskExecution, error: java.lang.Exception) {
        logger.info("Error occurs in execution ${te.id} of Task ${te.task.name}(${te.taskId}): $error")
    }

    override fun onActionStart(te: TaskExecution, actionIndex: Int) {
        logger.info("Execution ${te.id} of action (${te.task.actions[actionIndex].name} in Task ${te.task.name}(${te.taskId}) started")
    }

    override fun onActionComplete(te: TaskExecution, actionIndex: Int, result: ExecutionActionDetail) {
        logger.info("Execution ${te.id} of action (${te.task.actions[actionIndex].name} in Task ${te.task.name}(${te.taskId}) ${if (result.isSucceeded) "succeeded" else "failed"}")
    }

    override fun onActionError(te: TaskExecution, actionIndex: Int, error: java.lang.Exception) {
        logger.info("Error occurs in execution ${te.id} of action (${te.task.actions[actionIndex].name} in Task ${te.task.name}(${te.taskId}): $error")
    }

    override fun onDataLoadStart(te: TaskExecution) {
        logger.info("Data loading of execution ${te.id} of Task ${te.task.name}(${te.taskId}) started")
    }

    override fun onDataLoadComplete(te: TaskExecution, isLoadingSuccessful: Boolean) {
        logger.info("Data loading of execution ${te.id} of Task ${te.task.name}(${te.taskId}) ${if (isLoadingSuccessful) "succeeded" else "failed"}")
    }

}
