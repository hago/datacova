/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.worker.execution

import com.hagoapp.datacova.lib.action.TaskAction
import com.hagoapp.datacova.lib.execution.DataMessage

interface TaskExecutionActionWatcher {
    /**
     * used for action executor to notify worker that some error occurs but will continue
     * the watcher returns true as "acknowledged, continue", false for "acknowledged, but stop now"
     */
    fun onError(action: TaskAction, error: Exception): Boolean
    fun onMessage(action: TaskAction, msg: String)
    fun onDataMessage(action: TaskAction, lineNo: Int, msg: DataMessage)
    fun onProgressUpdate(action: TaskAction, progress: Float)
}
