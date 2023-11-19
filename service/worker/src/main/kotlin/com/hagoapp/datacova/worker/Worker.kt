/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker

import com.hagoapp.datacova.file.FileStoreFactory
import com.hagoapp.datacova.lib.action.TaskAction
import com.hagoapp.datacova.lib.execution.*
import com.hagoapp.datacova.utility.StackTraceWriter
import com.hagoapp.datacova.worker.execution.TaskActionExecutorFactory
import com.hagoapp.datacova.worker.execution.TaskExecutionActionWatcher
import com.hagoapp.datacova.worker.execution.TaskExecutionGroupWatcher
import com.hagoapp.datacova.worker.execution.TaskExecutionWatcher
import com.hagoapp.f2t.DataTable
import com.hagoapp.f2t.FileColumnDefinition
import com.hagoapp.f2t.FileParser
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.util.*

class Worker(private val taskExec: TaskExecution) : TaskExecutionActionWatcher, TaskExecutionWatcher {
    private val logger = LoggerFactory.getLogger(Worker::class.java)
    private val detail = ExecutionDetail(taskExec)
    private var currentActionDetail: ExecutionActionDetail? = null
    private var currentActionIndex: Int = 0
    private val watcherGroup = TaskExecutionGroupWatcher(this)
    private val config = Application.oneApp().config
    private val localTempDir = System.clearProperty("java.io.tmpdir")

    fun addWatcher(watcher: TaskExecutionWatcher): Worker {
        watcherGroup.addWatcher(watcher)
        return this
    }

    init {
        addWatcher(this)
    }

    private fun createTempFile(fileInfo: ExecutionFileInfo): String {
        val fs = FileStoreFactory.createFileStore(config.fileStore)
        val f = File(localTempDir, UUID.randomUUID().toString()).absolutePath
        fs.getFile(fileInfo.fileId).use { reader ->
            FileOutputStream(f).use { writer ->
                val buffer = ByteArray(1024 * 1024 * 5)
                while (true) {
                    val i = reader.read(buffer, 0, buffer.size)
                    if (i < 0) {
                        break
                    }
                    writer.write(buffer, 0, i)
                }
            }
        }
        return f
    }

    fun execute() {
        detail.startTiming()
        watcherGroup.onStart(taskExec)
        val dt: DataTable<FileColumnDefinition>
        watcherGroup.onDataLoadStart(taskExec)
        val tempFile = createTempFile(taskExec.fileInfo)
        try {
            taskExec.fileInfo.fileInfo.filename = tempFile
            val parser = FileParser(taskExec.fileInfo.fileInfo)
            dt = parser.extractData()
            watcherGroup.onDataLoadComplete(taskExec, true)
            detail.lineCount = dt.rows.size
        } catch (ex: Exception) {
            val msg =
                "Data source loading fail, abort execution ${taskExec.id} of task ${taskExec.task.name}(${taskExec.taskId})"
            logger.error(msg)
            watcherGroup.onError(taskExec, ex)
            watcherGroup.onDataLoadComplete(taskExec, false)
            detail.dataLoadingError = ex
            detail.endTiming()
            watcherGroup.onComplete(taskExec, detail)
            return
        } finally {
            File(tempFile).delete()
        }
        for (i in 0 until taskExec.task.actions.size) {
            currentActionIndex = i
            val action = taskExec.task.actions[i]
            currentActionDetail = detail.addActionDetail(i, action)
            watcherGroup.onActionStart(taskExec, i)
            if (!action.isEnabled) {
                logger.info("action $i: ${action.name} is skipped because it's disabled")
                continue
            }
            try {
                val executor = TaskActionExecutorFactory.createTaskActionExecutor(action)
                executor.locale = taskExec.task.extra.locale
                executor.watcher = this
                executor.execute(taskExec, action, dt)
                currentActionDetail!!.end()
                watcherGroup.onActionComplete(taskExec, i, currentActionDetail!!)
                if ((i < taskExec.task.actions.size - 1) && !executor.mayContinueWhenDone()) {
                    logger.info("action $i: ${action.name} completed, and it prevents following actions to proceed")
                    break
                }
            } catch (ex: Exception) {
                logger.error("Error occurs in action $i: ${action.name} of execution ${taskExec.id}: ${ex.message}")
                StackTraceWriter.write(ex, logger)
                currentActionDetail!!.error = ex
                currentActionDetail!!.end()
                watcherGroup.onActionComplete(taskExec, i, currentActionDetail!!)
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
        watcherGroup.onComplete(taskExec, detail)
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
        logger.debug("task execution {} of Task {}({}) started", te.id, te.task.name, te.taskId)
    }

    override fun onComplete(te: TaskExecution, result: ExecutionDetail) {
        logger.debug("task execution {} of Task {}({}) completed", te.id, te.task.name, te.taskId)
    }

    override fun onError(te: TaskExecution, error: java.lang.Exception) {
        logger.debug("Error occurs in execution {} of Task {}({}): {}", te.id, te.task.name, te.taskId, error.message)
    }

    override fun onActionStart(te: TaskExecution, actionIndex: Int) {
        logger.debug(
            "Execution {}, action {} in task {}({}) started",
            te.id,
            te.task.actions[actionIndex].name,
            te.task.name,
            te.taskId
        )
    }

    override fun onActionComplete(te: TaskExecution, actionIndex: Int, result: ExecutionActionDetail) {
        logger.debug(
            "Execution {}, action {} in task {}({}) {}",
            te.id,
            te.task.actions[actionIndex].name,
            te.task.name,
            te.taskId,
            if (result.isSucceeded) "succeeded" else "failed"
        )
    }

    override fun onActionError(te: TaskExecution, actionIndex: Int, error: java.lang.Exception) {
        logger.debug(
            "Action error occurs in execution {}, action {} in Task {}({}): {}",
            te.id, te.task.actions[actionIndex].name, te.task.name, te.task.id, error.message
        )
    }

    override fun onDataLoadStart(te: TaskExecution) {
        logger.debug("Data loading of execution {} of Task {}({}) started", te.id, te.task.name, te.taskId)
    }

    override fun onDataLoadComplete(te: TaskExecution, isLoadingSuccessful: Boolean) {
        logger.debug(
            "Data loading of execution {} of task {}({}) {}", te.id, te.task.name, te.taskId,
            if (isLoadingSuccessful) "succeeded" else "failed"
        )
    }

}