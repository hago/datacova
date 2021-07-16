/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.executor

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.config.init.CoVaConfig
import com.hagoapp.datacova.dispatcher.DispatcherInvoker
import com.hagoapp.datacova.entity.execution.ExecutionActionDetail
import com.hagoapp.datacova.entity.execution.ExecutionDetail
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.execution.TaskExecutionWatcher
import com.hagoapp.datacova.web.WebManager
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class Executor private constructor() : TaskExecutionWatcher {
    companion object {
        private val executor = if (CoVaConfig.getConfig().executor == null) null else Executor()

        fun getExecutor(): Executor? {
            return executor
        }
    }

    private val lock = ReentrantReadWriteLock()
    private val config = CoVaConfig.getConfig().executor
    private val logger = CoVaLogger.getLogger()
    private val statuses = ConcurrentHashMap<Int, Status>()
    private val runningWorkerCount: Int
        get() {
            lock.read { return statuses.size }
        }
    private val dispatcher = DispatcherInvoker(config)
    private var heartbeatFailCount = 0
    private val timer = Timer("heartbeat", true)
    private val task = object : TimerTask() {
        override fun run() {
            if (dispatcher.heartbeat()) {
                heartbeatFailCount = 0
            } else {
                heartbeatFailCount++
                logger.error("heartbeat failed $heartbeatFailCount time${if (heartbeatFailCount > 1) "s" else ""}")
            }
        }

    }

    fun start() {
        if (!dispatcher.register()) {
            return
        }
        timer.schedule(task, 60000L, 60000L)
    }

    fun stop(force: Boolean = false) {
        logger.info("Stop Execution Service")
        timer.cancel()
        WebManager.getManager(config, listOf()).shutDownWebServer()
        if (!force) {
            while (true) {
                val n = getWorkerCount()
                if (n == 0) {
                    break
                }
                logger.info("waiting for {} running worker{} to stop", n, if (n > 1) "s" else "")
                Thread.sleep(2000)
            }
        }
        logger.info("Execution Service Stopped")
    }

    private fun getWorkerCount(): Int {
        lock.read { return runningWorkerCount }
    }

    override fun onStart(te: TaskExecution) {
        lock.write { statuses.put(te.id, Status(te.id, 0f, Instant.now().toEpochMilli())) }
    }

    override fun onComplete(te: TaskExecution, result: ExecutionDetail) {
        lock.write { statuses.remove(te.id) }
    }

    override fun onError(te: TaskExecution, error: Exception) {
        lock.write { statuses.remove(te.id) }
    }

    override fun onActionComplete(te: TaskExecution, actionIndex: Int, result: ExecutionActionDetail) {
        val p = (2 + actionIndex).toFloat() / calcSteps(te)
        lock.write {
            statuses.compute(te.id) { _, existed ->
                if (existed == null) Status(te.id, p, Instant.now().toEpochMilli())
                else {
                    existed.progress = p
                    existed
                }
            }
        }
    }

    override fun onDataLoadComplete(te: TaskExecution, isLoadingSuccessful: Boolean) {
        val p = 1f / calcSteps(te)
        lock.write {
            statuses.compute(te.id) { _, existed ->
                if (existed == null) Status(te.id, p, Instant.now().toEpochMilli())
                else {
                    existed.progress = p
                    existed
                }
            }
        }
    }

    private fun calcSteps(te: TaskExecution): Float {
        return te.task.actions.size.toFloat() + 1f
    }

    fun getExecutionStatuses(): List<Status> {
        lock.read {
            return statuses.values.map { it }
        }
    }
}
