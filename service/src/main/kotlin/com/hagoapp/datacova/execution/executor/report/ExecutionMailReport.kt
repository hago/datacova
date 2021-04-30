/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.executor.report

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.entity.execution.ExecutionDetail
import com.hagoapp.datacova.entity.execution.TaskExecution

class ExecutionMailReport: ExecutionReport {
    private val logger = CoVaLogger.getLogger()

    override fun sendReport(execution: TaskExecution, detail: ExecutionDetail) {
        logger.debug("send report mail")
    }
}
