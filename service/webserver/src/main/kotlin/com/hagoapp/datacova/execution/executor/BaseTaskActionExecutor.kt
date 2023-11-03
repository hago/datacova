/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.executor

import com.hagoapp.datacova.execution.TaskExecutionActionWatcher
import com.hagoapp.datacova.lib.action.TaskAction
import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.f2t.ColumnDefinition
import com.hagoapp.f2t.DataTable
import java.util.*

abstract class BaseTaskActionExecutor {
    /**
     * if any exception is thrown during execution, the action will be marked as fail
     * to send error / message, use the watcher
     */
    var watcher: TaskExecutionActionWatcher? = null
    private var loc: Locale = Locale.getDefault()
    var locale: Locale
        get() = loc
        set(value) {
            loc = value
        }

    abstract fun execute(taskExecution: TaskExecution, action: TaskAction, data: DataTable<out ColumnDefinition>)

    abstract fun getActionType(): Int

    /**
     * This method is called to determine whether follow-up executors should continue or abort. The scenario is that
     * some verification executor failed some test on data but there's no error, any follow-up ingest or distribute
     * actions should not proceed because the data is not good enough to go.
     */
    open fun mayContinueWhenDone(): Boolean {
        return true
    }
}
