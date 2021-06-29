/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.executor

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.dispatcher.DispatcherInvoker
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.web.WebManager
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class Executor private constructor() {
    companion object {
        private val executor = if (CoVaConfig.getConfig().executor == null) null else Executor()
        private val lock = ReentrantReadWriteLock()

        fun getExecutor(): Executor? {
            return executor
        }
    }

    private val config = CoVaConfig.getConfig().executor
    private val logger = CoVaLogger.getLogger()
    private var runningWorkerCount = 0
        get() {
            lock.read { return field }
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

    fun workerStarts(t: TaskExecution) {
        lock.write { runningWorkerCount++ }
    }

    fun workerCompletes(t: TaskExecution) {
        lock.write { runningWorkerCount-- }
    }
}
