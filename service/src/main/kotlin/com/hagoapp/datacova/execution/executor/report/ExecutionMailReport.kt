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
import com.hagoapp.datacova.util.text.TemplateManager

class ExecutionMailReport : ExecutionReport {
    private val logger = CoVaLogger.getLogger()

    companion object {
        const val EXECUTION_REPORT_MAIL_TEMPLATE = "exec_report"
    }

    override fun sendReport(execution: TaskExecution, detail: ExecutionDetail) {
        val template = TemplateManager.getManager().getTemplate(EXECUTION_REPORT_MAIL_TEMPLATE)
        if (template == null) {
            logger.error("Execution mail report template configuration incorrect")
            return
        }

    }
}
