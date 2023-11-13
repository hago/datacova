/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility

import com.hagoapp.datacova.utility.text.FtlConfig
import com.hagoapp.datacova.utility.text.FtlManager
import freemarker.template.Template
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.io.StringWriter
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.*

class FtlManagerTest {

    private val locales = listOf(
        Locale.getDefault(),
        Locale.CHINA,
        Locale("es", "ES"),
        Locale.US,
        // this one has no templates, should fall back to system default
        Locale("ja", "JP"),
    )

    private val logger = LoggerFactory.getLogger(FtlManagerTest::class.java)

    @Test
    fun testTemplatesInResource() {
        val config = FtlConfig()
        config.isUseResource = true
        config.directory = "freemarker"
        val man = FtlManager.ResourceFtlManager(config)
        locales.forEach { locale ->
            val data = generateData(locale)
            val tpl = man.getTemplate("freemarker/demoTemplate", locale)
            render(tpl, data, locale)
        }
    }

//    @Test
//    fun testTemplatesInLocalDirectory() {
//        val config = TemplateConfig()
//        config.isUseResource = false
//        config.directory = "resources/freemarker"
//        val man = TemplateManager.ResourceTemplateManager(config)
//        val tpl = man.getTemplate("demoTemplate")
//        render(tpl)
//    }

    private fun render(tpl: Template?, data: Map<String, Any>, locale: Locale) {
        Assertions.assertNotNull(tpl)
        Assertions.assertTrue(tpl!!.locale.equals(locale) || tpl.locale.equals(Locale.getDefault()))
        StringWriter().use {
            tpl.process(data, it)
            logger.debug("rendered with {}: {}", locale, it.toString())
        }
    }

    private fun generateData(locale: Locale): Map<String, Any> {
        return mapOf(
            "date" to ZonedDateTime.now()
                .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.FULL).localizedBy(locale)),
            "weekDays" to DayOfWeek.values().map { d -> d.getDisplayName(TextStyle.FULL, locale) }
        )
    }
}
