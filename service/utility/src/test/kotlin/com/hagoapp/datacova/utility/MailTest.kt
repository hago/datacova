/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility

import com.hagoapp.datacova.utility.MailTest.Companion.MAIL_HOST
import com.hagoapp.datacova.utility.mail.MailConfig
import com.hagoapp.datacova.utility.mail.MailHelper
import com.hagoapp.datacova.utility.text.TextUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.io.FileInputStream
import javax.mail.internet.InternetAddress

@EnabledIfSystemProperty(named = MAIL_HOST, matches = ".*")
class MailTest {

    companion object {
        const val MAIL_HOST = "mail.host"
        const val MAIL_ATTACHMENT = "mail.attach"
        private const val MAIL_PORT = "mail.port"
        private const val MAIL_LOGIN = "mail.login"
        private const val MAIL_PWD = "mail.password"
        private const val MAIL_SSL = "mail.ssl"
        private const val MAIL_TO = "mail.ssl"
        private const val MAIL_CC = "mail.cc"
        private const val MAIL_BCC = "mail.bcc"
        private const val MAIL_TITLE = "Email Test|邮件测试|メールテスト|Pruebas de correo"
        private const val MAIL_CONTENT = """
            <h1>Email Test|邮件测试|メールテスト|Pruebas de correo</h1>
            <p>This is a test email sent by test automation of HagoApp.<p>
            <p>这是HagoApp的自动测试发出的测试邮件.<p>
            <p>HagoAppのテスト自動化で送信するテストメールです.<p>
            <p>Este es un correo electrónico de prueba enviado por la automatización de pruebas de HagoApp.<p>
            <br/>
            <p>Best regards</p>
            <p>Your Sincerely, HagoApp</p>
        """

        private val config = MailConfig()

        @BeforeAll
        @JvmStatic
        fun init() {
            val ssl = System.getProperty(MAIL_SSL, "1").toBoolean()
            with(config) {
                host = System.getProperty(MAIL_HOST)
                port = System.getProperty(MAIL_PORT, if (ssl) "465" else "25").toInt()
                user = System.getProperty(MAIL_LOGIN)
                password = System.getProperty(MAIL_PWD)
                from = "Automation Test<autotest@hagoapp.com>"
            }
        }

        data class Receipients(
            val to: List<String>,
            val cc: List<String>,
            val bcc: List<String>
        ) {
            companion object {
                fun parseLine(line: String): List<String> {
                    return line.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                }
            }
        }
    }

    @Test
    fun sendMail() {
        val mail = MailHelper(config)
        val rec = Receipients(
            Receipients.parseLine(System.getProperty(MAIL_TO, "")),
            Receipients.parseLine(System.getProperty(MAIL_CC, "")),
            Receipients.parseLine(System.getProperty(MAIL_BCC, ""))
        )
        mail.addRecipientList(rec.to.map { InternetAddress(it) })
        mail.addRecipientCCList(rec.cc.map { InternetAddress(it) })
        mail.addRecipientBCCList(rec.bcc.map { InternetAddress(it) })
        mail.setTitle(MAIL_TITLE)
        if (TextUtils.isHtml(MAIL_CONTENT)) {
            mail.setHtmlContent(MAIL_CONTENT)
        } else {
            mail.setTextContent(MAIL_CONTENT)
        }
        val att = System.getProperty(MAIL_ATTACHMENT)
        if (att != null) {
            mail.addAttachment(att)
            FileInputStream(att).use {
                mail.addAttachment("$att.stream", it)
            }
        }
        mail.sendMail()
    }
}
