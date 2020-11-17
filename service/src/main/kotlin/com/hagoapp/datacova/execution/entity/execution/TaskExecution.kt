/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.entity.execution

data class TaskExecution (
    val id: Int,
    val taskId: Int,
    val addBy: String,
    val addTime: Long,
    val startTime: Long,
    val endTime: Long?,
    val detail: ExecutionDetail?,
    val status: ExecutionStatus,
    val task: Task,
    val fileInfo: ExecutionFileInfo
)