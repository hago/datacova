/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher.server

import com.hagoapp.datacova.dispatcher.Application
import com.hagoapp.datacova.lib.data.TaskExecutionData
import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.lib.ingest.TaskActionIngest
import com.hagoapp.datacova.message.RegisterMessage
import com.hagoapp.datacova.message.TaskExecutionMessage
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * The states information holder for dispatch server.
 *
 * @constructor Create empty Server state
 */
object ServerState {
    private val logger = LoggerFactory.getLogger(ServerState::class.java)
    private val speakers = ConcurrentHashMap<String, WorkerSpeaker>()
    private val workerStates = ConcurrentHashMap<String, WorkerStatus>()
    private val taskExecutionsInProcess = ConcurrentHashMap<Int, TaskExecution>()
    private const val MONITOR_INTERVAL = 5000L

    init {
        logger.debug("starting monitor timer")
        Timer().schedule(
            object : TimerTask() {
                override fun run() {
                    logger.debug(
                        "internal status: {} workers registered, {} task executions in processing",
                        speakers.size,
                        taskExecutionsInProcess.size
                    )
                    logger.debug("executions in process: {}", taskExecutionsInProcess.keys.sorted())
                    workerStates.forEach { (speakerId, status) ->
                        if (status.taskExecution != null) {
                            logger.debug(
                                "worker {} is running execution {}({}) since {}",
                                speakerId,
                                status.taskExecution!!.id,
                                status.taskExecution!!.task.name,
                                DateTimeFormatter.ISO_DATE_TIME.format(
                                    ZonedDateTime.ofInstant(
                                        Instant.ofEpochMilli(
                                            status.issueTime!!
                                        ), ZoneId.of("UTC")
                                    )
                                )
                            )
                        } else {
                            logger.debug("worker {} is not assigned", speakerId)
                        }
                    }
                }
            }, 100L, MONITOR_INTERVAL
        )
    }

    fun workerRegister(workerSpeaker: WorkerSpeaker, registerMessage: RegisterMessage): String? {
        val state = WorkerStatus(workerSpeaker.id, registerMessage.name, null, registerMessage.jobTime)
        if (registerMessage.taskExecutionJob != null) {
            val te = TaskExecution.loadFromJson(registerMessage.taskExecutionJob)
            if (te == null) {
                logger.error(
                    "Registration failed for speaker {} socket {}: invalid task execution",
                    registerMessage.name,
                    workerSpeaker.id
                )
                return null
            } else {
                state.taskExecution = te
            }
        }
        speakers[workerSpeaker.id] = workerSpeaker
        workerStates[workerSpeaker.id] = state
        if (registerMessage.name == null) {
            registerMessage.name = UUID.randomUUID().toString()
        }
        return registerMessage.name
    }

    fun findAvailableWorker(): WorkerSpeaker? {
        val id = workerStates.values.firstOrNull { it.taskExecution == null }?.speakerId
        return if (id == null) null else speakers[id]
    }

    fun issueJob(speaker: WorkerSpeaker, taskExecution: TaskExecution) {
        val conMap = TaskExecutionData(Application.config.db).use { db ->
            db.getIngestDbConfigStrings(
                taskExecution.task.actions.filterIsInstance<TaskActionIngest>().map { it.connectionId })
        }
        taskExecutionsInProcess[taskExecution.id] = taskExecution
        workerStates[speaker.id] = WorkerStatus(
            speaker.id,
            speaker.id,
            taskExecution,
            Instant.now().toEpochMilli()
        )
        val msg = TaskExecutionMessage(taskExecution.toJson(), conMap)
        speaker.sendMessage(msg)
        logger.debug("execution {} is issued to worker {}", taskExecution.id, speaker.id)
    }

    fun findNewTaskExecutions(list: List<TaskExecution>): List<TaskExecution> {
        return list.filterNot { taskExecutionsInProcess.containsKey(it.id) }
    }

    fun jobDone(taskExecution: TaskExecution) {
        val speakerId = workerStates.firstNotNullOfOrNull {
            if (it.value.taskExecution?.id != taskExecution.id) null
            else it.key
        }
        if (speakerId == null) {
            logger.warn("attempt to log task execution {}'s completion but not found", taskExecution.id)
        } else {
            val te = workerStates.getValue(speakerId)
            te.taskExecution = null
            te.issueTime = null
            taskExecutionsInProcess.remove(taskExecution.id)
        }
    }
}
