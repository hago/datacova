/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor

import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.entity.action.TaskActionType
import com.hagoapp.f2t.DataTable

class IngestExecutor : BaseTaskActionExecutor() {
    override fun execute(action: TaskAction, data: DataTable) {
        TODO("Not yet implemented")
    }

    override fun getActionType(): TaskActionType {
        return TaskActionType.DatabaseIngest
    }

}
