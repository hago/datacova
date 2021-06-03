/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.ExecutorManager
import com.hagoapp.datacova.data.execution.TaskExecutionData
import com.hagoapp.datacova.entity.execution.TaskExecution
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class Dispatcher {
    companion object {
        private val dispatcher = Dispatcher()

        fun get(): Dispatcher {
            return dispatcher
        }
    }

    private val logger = CoVaLogger.getLogger()
    private val queue = LinkedBlockingQueue<TaskExecution>(1000)
    private val runner = Thread {
        while (true) {
            if (queue.isEmpty()) {
                Thread.sleep(1000)
            } else {
                val x = ExecutorManager.getManager().findLessLoadedExecutor()
                if (x == null) {
                    logger.warn("No available executor")
                    Thread.sleep(10000)
                } else {
                    val te = queue.poll()
                    try {
                        ExecutionDispatch(x).dispatch(te)
                    } catch (e: CoVaException) {
                        logger.error(e.message)
                        queue.put(te)
                    }
                }
            }
        }
    }

    fun startDispatcher() {
        fetchExecutions()
        runner.isDaemon = true
        runner.start()
    }

    private fun fetchExecutions(): Int {
        val l = TaskExecutionData().loadQueueingTaskExecution()
        if (l.isNotEmpty()) {
            queue.addAll(l)
        }
        return l.size
    }

    fun executionComing(te: TaskExecution) {
        if (queue.isEmpty()) {
            queue.offer(te, 2, TimeUnit.SECONDS)
        }
    }
}
