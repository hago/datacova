/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.execution

import com.hagoapp.datacova.lib.execution.ExecutionActionDetail
import com.hagoapp.datacova.lib.execution.ExecutionDetail
import com.hagoapp.datacova.lib.execution.TaskExecution
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

class TaskExecutionGroupWatcher(private val watchers: MutableSet<TaskExecutionWatcher>) : TaskExecutionWatcher {

    constructor() : this(mutableListOf())

    constructor(vararg manyWatchers: TaskExecutionWatcher) : this(manyWatchers.toMutableSet())

    constructor(watcherList: List<TaskExecutionWatcher>) : this(watcherList.toMutableSet())

    private val watcherMethods = TaskExecutionWatcher::class.java.methods.associateBy { it.name }
    private val logger = LoggerFactory.getLogger(TaskExecutionGroupWatcher::class.java)

    fun addWatcher(watcher: TaskExecutionWatcher) {
        watchers.add(watcher)
    }

    private fun callMethod(method: Method, vararg params: Any?) {
        watchers.forEach { w ->
            Runnable {
                try {
                    method.invoke(w, *params)
                } catch (e: Throwable) {
                    logger.error("Error {} in TaskExecutionWatcher.{} for {}", e.message, method.name, w::class.java)
                }
            }.run()
        }
    }

    override fun onStart(te: TaskExecution) {
        callMethod(watcherMethods.getValue("onStart"), te)
    }

    override fun onComplete(te: TaskExecution, result: ExecutionDetail) {
        callMethod(watcherMethods.getValue("onComplete"), te, result)
    }

    override fun onError(te: TaskExecution, error: Exception) {
        callMethod(watcherMethods.getValue("onError"), te, error)
    }

    override fun onActionStart(te: TaskExecution, actionIndex: Int) {
        callMethod(watcherMethods.getValue("onActionStart"), te, actionIndex)
    }

    override fun onActionComplete(te: TaskExecution, actionIndex: Int, result: ExecutionActionDetail) {
        callMethod(watcherMethods.getValue("onActionComplete"), te, actionIndex, result)
    }

    override fun onActionError(te: TaskExecution, actionIndex: Int, error: Exception) {
        callMethod(watcherMethods.getValue("onActionError"), te, actionIndex, error)
    }

    override fun onDataLoadStart(te: TaskExecution) {
        callMethod(watcherMethods.getValue("onDataLoadStart"), te)
    }

    override fun onDataLoadComplete(te: TaskExecution, isLoadingSuccessful: Boolean) {
        callMethod(watcherMethods.getValue("onDataLoadComplete"), te, isLoadingSuccessful)
    }
}
