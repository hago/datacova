/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher

import com.hagoapp.datacova.dispatcher.server.ServerState
import com.hagoapp.datacova.dispatcher.server.WorkerSpeaker
import com.hagoapp.datacova.lib.data.TaskExecutionData
import com.hagoapp.datacova.lib.execution.TaskExecution
import org.slf4j.LoggerFactory
import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean

object DispatchServer {

    private val logger = LoggerFactory.getLogger(DispatchServer::class.java)
    private val config = Application.config
    private val shouldClose = AtomicBoolean(false)

    fun start() {
        Thread { dispatchData() }.start()
        ServerSocket(config.port).use {
            logger.info("socket server started")
            while (!shouldClose.get()) {
                val sk = it.accept()
                val speaker = WorkerSpeaker(sk)
                Thread(speaker).start()
            }
            logger.info("socket server, closed")
        }
    }

    fun stop() {
        shouldClose.set(true)
    }

    private const val FREE_INTERVAL = 5000L
    private fun dispatchData() {
        val executions = mutableListOf<TaskExecution>()
        val sleeper = Object()
        while (shouldClose.get()) {
            if (executions.isEmpty()) {
                executions.addAll(TaskExecutionData(Application.config.db).use {
                    it.loadQueueingTaskExecution()
                })
            }
            if (executions.isEmpty()) {
                logger.debug("no task execution to run, sleep")
                sleeper.wait(FREE_INTERVAL)
                continue
            }
            val speaker = ServerState.findAvailableWorker()
            if (speaker == null) {
                logger.debug("no available worker")
                sleeper.wait(FREE_INTERVAL)
                continue
            }
            val te = executions.removeAt(0)
            ServerState.issueJob(speaker, te)
        }
    }
}
