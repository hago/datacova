/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.MailConfig
import com.hagoapp.datacova.config.TestConfig
import com.hagoapp.datacova.config.TestMailConfig
import com.hagoapp.datacova.util.MailHelper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.io.File
import javax.mail.internet.InternetAddress

/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

class MailTest {

    companion object {
        private const val DEFAULT_CONFIG_FILE = "config.json"
        private const val DEFAULT_TEST_CONFIG_FILE = "testconfig.json"
        private const val CONFIG_FILE_PROPERTY = "ccova.config"
    }

    private lateinit var configFile: String
    private lateinit var config: MailConfig
    private lateinit var testConfig: TestMailConfig

    @BeforeAll
    private fun init() {
        configFile = System.getProperty(CONFIG_FILE_PROPERTY) ?: DEFAULT_CONFIG_FILE
        if (!File(configFile).exists()) {
            throw CoVaException("$configFile not existed")
        }
        CoVaConfig.loadConfig(configFile)
        config = CoVaConfig.getConfig().mail
        testConfig = TestConfig.load(DEFAULT_TEST_CONFIG_FILE).mail
    }

    @Test
    fun sendMail() {
        val mail = MailHelper(config)
        mail.addRecipientList(testConfig.to.map { InternetAddress(it) })
        mail.addRecipientCCList(testConfig.cc.map { InternetAddress(it) })
        mail.addRecipientBCCList(testConfig.bcc.map { InternetAddress(it) })
        mail.setTitle(testConfig.title)
        mail.sendMail()
    }
}
