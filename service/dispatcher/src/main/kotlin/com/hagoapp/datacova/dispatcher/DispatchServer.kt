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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
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
        logger.info("dispatchData")
        val executions = mutableListOf<TaskExecution>()
        while (!shouldClose.get()) {
            val speaker = ServerState.findAvailableWorker()
            logger.info("speaker {}", speaker)
            if (speaker == null) {
                logger.debug("no available worker")
                runBlocking { delay(FREE_INTERVAL) }
                continue
            }
            if (executions.isEmpty()) {
                executions.addAll(findTaskExecutionToHandle())
            }
            if (executions.isEmpty()) {
                logger.debug("no task execution to run, sleep")
                runBlocking { delay(FREE_INTERVAL) }
                continue
            }
            val te = executions.removeAt(0)
            ServerState.issueJob(speaker, te)
        }
    }

    private fun findTaskExecutionToHandle(): List<TaskExecution> {
        val l = TaskExecutionData(Application.config.db).use {
            it.loadQueueingTaskExecution()
        }
        return ServerState.findNewTaskExecutions(l)
    }
}
