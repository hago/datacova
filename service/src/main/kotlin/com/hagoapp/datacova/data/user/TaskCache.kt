/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.user

import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.data.workspace.TaskData
import com.hagoapp.datacova.entity.task.Task

class TaskCache {
    companion object {

        private const val TASK_LIST = "TASK_LIST"

        @JvmStatic
        fun listTasks(id: Int): List<Task> {
            val token = object : TypeToken<List<Task>>() {}
            val l = RedisCacheReader.readCachedData(
                TASK_LIST, 3600,
                object : RedisCacheReader.GenericLoader<List<Task>> {
                    override fun perform(vararg params: Any?): List<Task> {
                        return if (params.isEmpty()) listOf() else
                            TaskData().listWorkspaceTask(params[0] as Int)
                    }
                }, token.type, id
            )
            return l ?: listOf()
        }
    }
}
