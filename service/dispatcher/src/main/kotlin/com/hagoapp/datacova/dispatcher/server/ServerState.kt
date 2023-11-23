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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object ServerState {
    private val logger = LoggerFactory.getLogger(ServerState::class.java)
    private val speakers = ConcurrentHashMap<String, WorkerSpeaker>()
    private val workerStates = ConcurrentHashMap<String, WorkerStatus>()
    private val taskExecutionsInProcess = ConcurrentHashMap<Int, TaskExecution>()

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
        taskExecutionsInProcess[taskExecution.id] = taskExecution
        workerStates[speaker.id] = WorkerStatus(
            speaker.id,
            speaker.id,
            taskExecution,
            Instant.now().toEpochMilli()
        )
        val conMap = TaskExecutionData(Application.config.db).use { db ->
            db.getIngestDbConfigStrings(
                taskExecution.task.actions.filterIsInstance<TaskActionIngest>().map { it.connectionId })
        }
        val msg = TaskExecutionMessage(taskExecution.toJson(), conMap)
        speaker.sendMessage(msg)
    }

    fun findNewTaskExecutions(list: List<TaskExecution>): List<TaskExecution> {
        return list.filter { !taskExecutionsInProcess.contains(it.id) }
    }
}
