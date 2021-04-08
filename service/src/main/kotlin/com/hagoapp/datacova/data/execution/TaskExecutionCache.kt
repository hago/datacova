/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.data.execution

import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.entity.execution.TaskExecution

class TaskExecutionCache {
    companion object {

        private const val TASK_EXECUTION = "TASK_EXECUTION"

        @JvmStatic
        fun getExecutionOfTask(taskId: Int): TaskExecution? {
            return RedisCacheReader.readCachedData(
                TASK_EXECUTION,
                3600,
                object : RedisCacheReader.GenericLoader<TaskExecution> {
                    override fun perform(vararg params: Any?): TaskExecution? {
                        val id = params[0].toString().toInt()
                        return TaskExecutionData().getTaskExecution(id)
                    }
                },
                TaskExecution::class.java,
                taskId
            )
        }
    }
}