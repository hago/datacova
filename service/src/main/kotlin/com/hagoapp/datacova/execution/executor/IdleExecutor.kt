/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.entity.action.idle.TaskActionIdle
import com.hagoapp.datacova.entity.action.idle.TaskActionIdle.TASK_ACTION_TYPE_IDLE
import com.hagoapp.f2t.DataTable
import java.security.SecureRandom

class IdleExecutor : BaseTaskActionExecutor() {
    override fun execute(action: TaskAction, data: DataTable) {
        if (action !is TaskActionIdle) {
            throw CoVaException()
        }
        val config = action.configuration
        watcher?.onProgressUpdate(action, 0f)
        var inc = 1f / config.stepCount.toFloat()
        SecureRandom.getInstanceStrong()
            .longs(config.stepCount.toLong(), config.maxMilliSeconds, config.maxMilliSeconds).forEach { number ->
                Thread.sleep(number)
                watcher?.onProgressUpdate(action, inc)
                inc += inc
            }
        watcher?.onProgressUpdate(action, 100f)
    }

    override fun getActionType(): Int {
        return TASK_ACTION_TYPE_IDLE
    }
}