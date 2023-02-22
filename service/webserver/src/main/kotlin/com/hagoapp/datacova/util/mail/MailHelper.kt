/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.mail

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.MailConfig
import org.slf4j.LoggerFactory
import java.util.*
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.*

class MailHelper(val config: MailConfig) {

    private val logger = LoggerFactory.getLogger(MailHelper::class.java)

    private val recipients = mutableListOf<InternetAddress>()
    private val recipientsCC = mutableListOf<InternetAddress>()
    private val recipientsBCC = mutableListOf<InternetAddress>()
    private var title = ""
    private var encoding = "UTF-8"
    private var isHtml = false
    private var content = ""
    private val attachments = mutableListOf<String>()
    val recipientsEmpty: Boolean
        get() {
            return recipients.isEmpty() && recipientsCC.isEmpty() && recipientsBCC.isEmpty()
        }

    constructor() : this(CoVaConfig.getConfig().mail)

    fun addRecipient(address: InternetAddress): MailHelper {
        recipients.add(address)
        return this
    }

    fun addRecipientCC(address: InternetAddress): MailHelper {
        recipientsCC.add(address)
        return this
    }

    fun addRecipientBCC(address: InternetAddress): MailHelper {
        recipientsBCC.add(address)
        return this
    }

    fun addRecipientList(addresses: List<InternetAddress>): MailHelper {
        recipients.addAll(0, addresses)
        return this
    }

    fun addRecipientCCList(addresses: List<InternetAddress>): MailHelper {
        recipientsCC.addAll(0, addresses)
        return this
    }

    fun addRecipientBCCList(addresses: List<InternetAddress>): MailHelper {
        recipientsBCC.addAll(0, addresses)
        return this
    }

    fun setTitle(value: String): MailHelper {
        title = value
        return this
    }

    fun setHtmlContent(value: String): MailHelper {
        content = value
        isHtml = true
        return this
    }

    fun setTextContent(value: String): MailHelper {
        content = value
        isHtml = false
        return this
    }

    fun addAttachment(attachment: String): MailHelper {
        attachments.add(attachment)
        return this
    }

    fun sendMail() {
        if (recipients.isEmpty() && recipientsCC.isEmpty() && recipientsBCC.isEmpty()) {
            logger.info("No recipients found, email sending aborted")
            return
        }
        val session = getSession()
        val msg = MimeMessage(session)
        if (config.from != null) {
            val fromAddresses = parseFrom(config.from)
            msg.setFrom(fromAddresses)
        }
        msg.setSubject(title, encoding)
        msg.addRecipients(Message.RecipientType.TO, recipients.toTypedArray())
        msg.addRecipients(Message.RecipientType.CC, recipientsCC.toTypedArray())
        msg.addRecipients(Message.RecipientType.BCC, recipientsBCC.toTypedArray())
        val textBody = MimeBodyPart()
        textBody.setContent(content, if (isHtml) "text/html; charset=$encoding" else "text/plain; charset=$encoding")
        val mailContent = MimeMultipart()
        mailContent.addBodyPart(textBody)
        attachments.forEach {
            val dh = DataHandler(FileDataSource(it))
            val mpAttach = MimeBodyPart()
            mpAttach.fileName = MimeUtility.encodeText(dh.name)
            mpAttach.dataHandler = dh
            mailContent.addBodyPart(mpAttach)
        }
        mailContent.setSubType("mix")
        msg.setContent(mailContent)
        if (config.user == null) Transport.send(msg) else Transport.send(msg, config.user, config.password)
    }

    private fun parseFrom(input: String): InternetAddress {
        return FromAddress.parse(input).toInternetAddress()
    }

    private fun getSession(): Session {
        val propMap = mutableMapOf(
            "mail.smtp.host" to config.host,
            "mail.smtp.port" to config.port,
            "mail.user" to config.user,
            "mail.from" to config.from
        )
        if (config.isSsl) {
            propMap["mail.smtp.socketFactory.port"] = config.port
            propMap["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        }
        val props = Properties()
        props.putAll(propMap.toList().filter { it.second != null }.toMap())
        return Session.getInstance(props)
    }

}
