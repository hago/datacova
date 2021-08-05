/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.executor

import com.hagoapp.datacova.config.init.CoVaConfig
import com.hagoapp.datacova.entity.execution.ExecutionDetail
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.execution.TaskExecutionWatcher
import com.hagoapp.datacova.util.mail.MailHelper
import com.hagoapp.datacova.util.text.TemplateManager
import java.io.StringWriter
import javax.mail.internet.InternetAddress

class ExecuteResultMailer : TaskExecutionWatcher {

    companion object {
        const val MAIL_START_BODY_TEMPLATE_NAME = "templates/execution/start/body"
        const val MAIL_START_TITLE_TEMPLATE_NAME = "templates/execution/start/title"
        const val MAIL_COMPLETE_BODY_TEMPLATE_NAME = "templates/execution/complete/body"
        const val MAIL_COMPLETE_TITLE_TEMPLATE_NAME = "templates/execution/complete/title"
    }

    override fun onStart(te: TaskExecution) {
        val tplMgr = if (CoVaConfig.getConfig().template == null) TemplateManager.getResourcedTemplateManager()
        else TemplateManager.getManager(CoVaConfig.getConfig().template)
        val body = StringWriter().use { writer ->
            val tpl = tplMgr.getTemplate(MAIL_START_BODY_TEMPLATE_NAME)
            tpl!!.process(mapOf("execution" to te), writer)
            writer.toString()
        }
        val title = StringWriter().use { writer ->
            val tpl = tplMgr.getTemplate(MAIL_START_TITLE_TEMPLATE_NAME)
            tpl!!.process(mapOf("execution" to te), writer)
            writer.toString()
        }
        val mail = MailHelper().setHtmlContent(body)
            .addRecipientList(te.task.extra.mailRecipients.map { InternetAddress(it) })
            .addRecipientCCList(te.task.extra.mailCCRecipients.map { InternetAddress(it) })
            .addRecipientBCCList(te.task.extra.mailBCCRecipients.map { InternetAddress(it) })
            .setTitle(title)
        mail.sendMail()
    }

    override fun onComplete(te: TaskExecution, result: ExecutionDetail) {
        val tplMgr = if (CoVaConfig.getConfig().template == null) TemplateManager.getResourcedTemplateManager()
        else TemplateManager.getManager(CoVaConfig.getConfig().template)
        val body = StringWriter().use { writer ->
            val tpl = tplMgr.getTemplate(MAIL_COMPLETE_BODY_TEMPLATE_NAME)
            tpl!!.process(mapOf("execution" to te, "result" to result), writer)
            writer.toString()
        }
        val title = StringWriter().use { writer ->
            val tpl = tplMgr.getTemplate(MAIL_COMPLETE_TITLE_TEMPLATE_NAME)
            tpl!!.process(mapOf("execution" to te, "result" to result), writer)
            writer.toString()
        }
        val mail = MailHelper().setHtmlContent(body)
            .addRecipientList(te.task.extra.mailRecipients.map { InternetAddress(it) })
            .addRecipientCCList(te.task.extra.mailCCRecipients.map { InternetAddress(it) })
            .addRecipientBCCList(te.task.extra.mailBCCRecipients.map { InternetAddress(it) })
            .setTitle(title)
        mail.sendMail()
    }
}
