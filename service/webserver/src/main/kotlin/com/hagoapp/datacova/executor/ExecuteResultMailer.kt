/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.executor

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.execution.TaskExecutionWatcher
import com.hagoapp.datacova.lib.execution.ExecutionDetail
import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.utility.mail.MailHelper
import com.hagoapp.datacova.utility.text.FtlManager
import java.io.StringWriter
import java.time.Duration
import javax.mail.internet.InternetAddress

class ExecuteResultMailer : TaskExecutionWatcher {

    companion object {
        const val MAIL_START_BODY_TEMPLATE_NAME = "execution/start/body"
        const val MAIL_START_TITLE_TEMPLATE_NAME = "execution/start/title"
        const val MAIL_COMPLETE_BODY_TEMPLATE_NAME = "execution/complete/body"
        const val MAIL_COMPLETE_TITLE_TEMPLATE_NAME = "execution/complete/title"
    }

    private lateinit var user: UserInfo

    override fun onStart(te: TaskExecution) {
        val tplMgr = FtlManager.getManager(CoVaConfig.getConfig().template)
        user = UserCache.getUser(te.addBy) ?: createUser(te.addBy)
        val body = StringWriter().use { writer ->
            val tpl = tplMgr.getTemplate(MAIL_START_BODY_TEMPLATE_NAME, te.task.extra.locale)
            tpl!!.process(mapOf("execution" to te, "user" to user), writer)
            writer.toString()
        }
        val title = StringWriter().use { writer ->
            val tpl = tplMgr.getTemplate(MAIL_START_TITLE_TEMPLATE_NAME, te.task.extra.locale)
            tpl!!.process(mapOf("execution" to te, "user" to user), writer)
            writer.toString()
        }
        val mail = MailHelper(CoVaConfig.getConfig().mail).setHtmlContent(body)
            .addRecipientList(te.task.extra.mailRecipients.map { InternetAddress(it) })
            .addRecipientCCList(te.task.extra.mailCCRecipients.map { InternetAddress(it) })
            .addRecipientBCCList(te.task.extra.mailBCCRecipients.map { InternetAddress(it) })
            .setTitle(title)
        mail.sendMail()
    }

    private fun createUser(id: Long): UserInfo {
        val u = UserInfo()
        u.id = id
        u.userId = "user $id"
        u.name = "user $id"
        return u
    }

    override fun onComplete(te: TaskExecution, result: ExecutionDetail) {
        val tplMgr = FtlManager.getManager(CoVaConfig.getConfig().template)
        val body = StringWriter().use { writer ->
            val tpl = tplMgr.getTemplate(MAIL_COMPLETE_BODY_TEMPLATE_NAME, te.task.extra.locale)
            tpl!!.process(
                mapOf(
                    "execution" to te,
                    "result" to result,
                    "user" to user,
                    "duration" to Duration.ofMillis(result.timeUsedMilliSeconds)
                ), writer
            )
            writer.toString()
        }
        val title = StringWriter().use { writer ->
            val tpl = tplMgr.getTemplate(MAIL_COMPLETE_TITLE_TEMPLATE_NAME, te.task.extra.locale)
            tpl!!.process(mapOf("execution" to te, "result" to result, "user" to user), writer)
            writer.toString()
        }
        val mail = MailHelper(CoVaConfig.getConfig().mail).setHtmlContent(body)
            .addRecipientList(te.task.extra.mailRecipients.map { InternetAddress(it) })
            .addRecipientCCList(te.task.extra.mailCCRecipients.map { InternetAddress(it) })
            .addRecipientBCCList(te.task.extra.mailBCCRecipients.map { InternetAddress(it) })
            .setTitle(title)
        mail.sendMail()
    }
}
