/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.task

data class TaskAction(
    val type: TaskActionType,
    val extra: TaskActionExtra,
    val name: String,
    val description: String
)