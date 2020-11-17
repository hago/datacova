/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.entity.execution

import com.hagoapp.datacova.task.TaskAction

data class ExecutionActionDetail (
    val     result: Boolean,
    val errors: List<Exception>,
    val messages: List<String>,
    val dataMessages: Map<Int, List<String>>,
    val action: TaskAction
)
