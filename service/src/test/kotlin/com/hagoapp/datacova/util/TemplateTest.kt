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
import java.io.StringWriter
import java.util.*

class TemplateTest {
    @Test
    fun testTemplateInResource() {
        val manager = TemplateManager.getResourceTemplateManager()
        val template = manager.getTemplate("user/register/activation", Locale.getDefault())
        Assertions.assertNotNull(template)
        val user = UserInfo()
        with (user) {
            userId = "test"
            name = "TestUser"
            email = "user@test.com"
        }
        val sw = StringWriter()
        template!!.process(user, sw)
        println(sw.toString())
    }

    @Test
    fun testTemplateInConf() {
        val conf = CoVaConfig.getConfig().template
        val manager = TemplateManager.getManager(File(conf.directory), conf.aliases)
        val template = manager.getTemplate("user/register/activation", Locale.getDefault())
        Assertions.assertNotNull(template)
        val user = UserInfo()
        with (user) {
            userId = "test"
            name = "TestUser"
            email = "user@test.com"
        }
        val sw = StringWriter()
        template!!.process(user, sw)
        println(sw.toString())
    }
}
