/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.util

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.util.text.TemplateManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.StringWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class TemplateTest {
    @Test
    fun testTemplateInResource() {
        val manager = TemplateManager.getResourcedTemplateManager()
        val template = manager.getTemplate("user/register/activation", Locale.getDefault())
        Assertions.assertNotNull(template)
        val user = UserInfo()
        with(user) {
            userId = "test"
            name = "TestUser"
            email = "user@test.com"
        }
        val sw = StringWriter()
        template!!.process(
            mapOf(
                "user" to user,
                "homepage" to "https://abc.com",
                "activateurl" to "https://def.com",
                "expire" to DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now())
            ), sw
        )
        println(sw.toString())
    }

    @Test
    fun testTemplateInConf() {
        val conf = CoVaConfig.getConfig().template
            ?: throw FileNotFoundException("template directory configuration from config not found")
        val manager = TemplateManager.getManager(conf)
        val template = manager.getTemplate("user/register/activation", Locale.getDefault())
        Assertions.assertNotNull(template)
        val user = UserInfo()
        with(user) {
            userId = "test"
            name = "TestUser"
            email = "user@test.com"
        }
        val sw = StringWriter()
        template!!.process(
            mapOf(
                "user" to user,
                "homepage" to "https://abc.com",
                "activateurl" to "https://def.com",
                "expire" to DateTimeFormatter.ISO_DATE_TIME.format(ZonedDateTime.now())
            ), sw
        )
        println(sw.toString())
    }
}
