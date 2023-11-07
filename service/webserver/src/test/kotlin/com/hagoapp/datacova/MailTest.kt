/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova

import com.hagoapp.datacova.MailTest.Companion.ENABLE_MAIl_TEST
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.utility.mail.MailConfig
import com.hagoapp.datacova.config.TestConfig
import com.hagoapp.datacova.config.TestMailConfig
import com.hagoapp.datacova.utility.mail.MailHelper
import com.hagoapp.datacova.utility.text.TextUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import java.io.File
import javax.mail.internet.InternetAddress

@EnabledIfSystemProperty(named = ENABLE_MAIl_TEST, matches = ".*")
class MailTest {

    companion object {
        const val ENABLE_MAIl_TEST = "cova.mailtest"
        private const val DEFAULT_CONFIG_FILE = "config.json"
        private const val DEFAULT_TEST_CONFIG_FILE = "testconfig.json"
        private const val CONFIG_FILE_PROPERTY = "ccova.config"
        private const val TEST_CONFIG_FILE_PROPERTY = "ccova.testconfig"
    }

    private val configFile: String = System.getProperty(CONFIG_FILE_PROPERTY) ?: DEFAULT_CONFIG_FILE
    private val config: MailConfig
    private val testConfig: TestMailConfig
    private val testConfigFile: String = System.getProperty(TEST_CONFIG_FILE_PROPERTY) ?: DEFAULT_TEST_CONFIG_FILE

    init {
        if (!File(configFile).exists()) {
            throw CoVaException("$configFile not existed")
        }
        CoVaConfig.loadConfig(configFile)
        config = CoVaConfig.getConfig().mail
        if (!File(testConfigFile).exists()) {
            throw CoVaException("$testConfigFile not existed")
        }
        testConfig = TestConfig.load(testConfigFile).mail
    }

    @Test
    fun sendMail() {
        val mail = MailHelper(config)
        mail.addRecipientList(testConfig.to.map { InternetAddress(it) })
        mail.addRecipientCCList(testConfig.cc.map { InternetAddress(it) })
        mail.addRecipientBCCList(testConfig.bcc.map { InternetAddress(it) })
        mail.setTitle(testConfig.title)
        if (TextUtils.isHtml(testConfig.body)) {
            mail.setHtmlContent(testConfig.body)
        } else {
            mail.setTextContent(testConfig.body)
        }
        mail.addAttachment(testConfigFile)
        mail.sendMail()
    }
}
