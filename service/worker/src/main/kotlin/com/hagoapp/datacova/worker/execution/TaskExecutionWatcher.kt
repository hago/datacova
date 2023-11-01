/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.worker.execution

import com.hagoapp.datacova.lib.execution.ExecutionActionDetail
import com.hagoapp.datacova.lib.execution.ExecutionDetail
import com.hagoapp.datacova.lib.execution.TaskExecution

interface TaskExecutionWatcher {
    fun onStart(te: TaskExecution) {}
    fun onComplete(te: TaskExecution, result: ExecutionDetail) {}
    fun onError(te: TaskExecution, error: Exception) {}
    fun onActionStart(te: TaskExecution, actionIndex: Int) {}
    fun onActionComplete(te: TaskExecution, actionIndex: Int, result: ExecutionActionDetail) {}
    fun onActionError(te: TaskExecution, actionIndex: Int, error: Exception) {}
    fun onDataLoadStart(te: TaskExecution) {}
    fun onDataLoadComplete(te: TaskExecution, isLoadingSuccessful: Boolean) {}
}