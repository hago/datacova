/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message

import com.hagoapp.datacova.message.TaskExecutionMessage.Companion.MESSAGE_JOB_ISSUING

/**
 * Task execution message, message from server to client to tell worker to run an execution.
 *
 * @property taskExecutionJob Json string of TaskExecution
 * @property connections map contains connections used by the task execution
 * @constructor Create empty Task execution message
 */
@WorkerMessage(type = MESSAGE_JOB_ISSUING)
class TaskExecutionMessage(val taskExecutionJob: String, val connections: Map<Int, String>) {

    companion object {
        const val MESSAGE_JOB_ISSUING: Byte = 2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskExecutionMessage

        if (taskExecutionJob != other.taskExecutionJob) return false
        if (connections != other.connections) return false

        return true
    }

    override fun hashCode(): Int {
        var result = taskExecutionJob.hashCode()
        result = 31 * result + connections.hashCode()
        return result
    }

}
