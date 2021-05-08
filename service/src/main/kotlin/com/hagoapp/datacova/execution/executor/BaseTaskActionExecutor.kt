/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.executor

import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.entity.task.Task
import com.hagoapp.datacova.execution.TaskExecutionActionWatcher
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

    abstract fun execute(task: Task, action: TaskAction, data: DataTable)

    abstract fun getActionType(): Int
}
