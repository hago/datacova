/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.lib.task.Task
import com.hagoapp.datacova.utility.redis.RedisCacheReader

class TaskCache {
    companion object {

        private const val TASK_LIST = "TASK_LIST"

        @JvmStatic
        fun listTasks(id: Int): List<Task> {
            val token = object : TypeToken<List<Task>>() {}
            val l = RedisCacheReader.readCachedData(
                CoVaConfig.getConfig().redis,
                TASK_LIST, 3600,
                { params ->
                    if (params.isEmpty()) listOf() else
                        TaskData(CoVaConfig.getConfig().database).getTasks(params[0] as Int)
                }, token.type, id
            )
            return l ?: listOf()
        }

        @JvmStatic
        fun clearWorkspaceTasks(workspaceId: Int) {
            RedisCacheReader.clearData(CoVaConfig.getConfig().redis, TASK_LIST, workspaceId)
        }

        @JvmStatic
        fun getTask(workspaceId: Int, taskId: Int): Task? {
            return listTasks(workspaceId).firstOrNull { it.id == taskId }
        }
    }
}
