/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.data.execution

import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.config.init.CoVaConfig
import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.data.redis.JedisManager
import com.hagoapp.datacova.entity.execution.TaskExecution

class TaskExecutionCache {
    companion object {

        const val TASK_EXECUTION_DEFAULT_PAGE_SIZE = 10
        private const val TASK_EXECUTION = "TASK_EXECUTION"
        private const val TASK_EXECUTION_OF_TASK = "TASK_EXECUTION_OF_TASK"
        private const val TASK_EXECUTION_LIST_OF_WORKSPACE = "TASK_EXECUTION_LIST_OF_WORKSPACE"

        @JvmStatic
        fun getExecutionOfTask(taskId: Int): TaskExecution? {
            return RedisCacheReader.readCachedData(
                TASK_EXECUTION_OF_TASK,
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

        @JvmStatic
        fun getExecutionsOfWorkspace(workspaceId: Int): List<TaskExecution> {
            return getExecutionsOfWorkspace(workspaceId, TASK_EXECUTION_DEFAULT_PAGE_SIZE)
        }

        @JvmStatic
        fun getExecutionsOfWorkspace(workspaceId: Int, size: Int): List<TaskExecution> {
            return getExecutionsOfWorkspace(workspaceId, 0, size)
        }

        @JvmStatic
        fun getExecutionsOfWorkspace(workspaceId: Int, start: Int, size: Int): List<TaskExecution> {
            val token = object : TypeToken<List<TaskExecution>>() {}
            return RedisCacheReader.readCachedData(
                TASK_EXECUTION_LIST_OF_WORKSPACE,
                3600,
                object : RedisCacheReader.GenericLoader<List<TaskExecution>> {
                    override fun perform(vararg params: Any?): List<TaskExecution> {
                        val id = params[0].toString().toInt()
                        val offset = params[1].toString().toInt()
                        val limit = params[2].toString().toInt()
                        return TaskExecutionData().getTaskExecutionsOfWorkspace(id, offset, limit)
                    }
                },
                token.type,
                workspaceId, start, size
            ) ?: listOf()
        }

        @JvmStatic
        fun clearWorkspaceTaskExecutions(workspaceId: Int) {
            JedisManager(CoVaConfig.getConfig().redis).use {
                val jedis = it.jedis
                val keys = jedis.keys("$TASK_EXECUTION_LIST_OF_WORKSPACE||$workspaceId*")
                if (keys.isNotEmpty()) {
                    jedis.del(*keys.toTypedArray())
                }
            }
        }

        @JvmStatic
        fun getTaskExecution(id: Int): TaskExecution? {
            return RedisCacheReader.readCachedData(
                TASK_EXECUTION,
                3600,
                object : RedisCacheReader.GenericLoader<TaskExecution> {
                    override fun perform(vararg params: Any?): TaskExecution? {
                        val execId = params[0].toString().toInt()
                        return TaskExecutionData().getTaskExecution(execId)
                    }
                },
                TaskExecution::class.java,
                id
            )
        }
    }
}