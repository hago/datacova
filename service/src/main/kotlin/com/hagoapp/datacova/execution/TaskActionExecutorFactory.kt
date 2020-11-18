/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.TaskAction

class TaskActionExecutorFactory {
    companion object {

        fun createTaskActionExecutor(action: TaskAction): BaseTaskActionExecutor {
            return when (action.type) {
//                TaskActionType.DataImporting -> ImportExecutor()
//                TaskActionType.Distributing -> DistributeExecutor()
//                TaskActionType.Verifying -> VerifyExecutor()
//                TaskActionType.IDLE -> IdleExecutor()
                else -> throw CoVaException("executor for action ${action.type} not implemented")
            }
        }
    }
}
