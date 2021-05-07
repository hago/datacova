/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.text

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.TemplateConfig
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import java.io.File
import java.io.IOException
import java.util.*

class TemplateManager private constructor() {
    companion object {

        private val manager = TemplateManager()

        fun getManager(): TemplateManager {
            return manager
        }
    }

    private val config: Configuration
    private val conf: TemplateConfig

    init {
        try {
            config = Configuration(Configuration.VERSION_2_3_28)
            config.defaultEncoding = "utf-8"
            config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
            config.logTemplateExceptions = false
            config.wrapUncheckedExceptions = true
            conf = CoVaConfig.getConfig().template
            config.setDirectoryForTemplateLoading(File(conf.directory))
        } catch (ex: IOException) {
            throw CoVaException("template init error", ex)
        }

    }

    fun getTemplate(name: String): Template? {
        return getTemplate(name, Locale.getDefault())
    }

    fun getTemplate(name: String, locale: Locale): Template? {
        val actualName = findTemplateFile(name, locale) ?: return null
        return config.getTemplate(actualName, locale)
    }

    private fun findTemplateFile(name: String, locale: Locale): String? {
        for (fileName in createPossibleTemplateFileNames(name, locale)) {
            if (File(conf.directory, fileName).exists()) {
                return fileName
            }
        }
        return null
    }

    private fun createPossibleTemplateFileNames(name: String, locale: Locale?): List<String> {
        val l = mutableListOf(
            "$name/${if (locale == null) "main" else "___${locale.displayName}"}.tpl",
            "$name/${if (locale == null) "default" else "___${locale.displayName}"}.tpl",
            "$name/main.tpl",
            "$name/default.tpl",
            "$name/${Locale.getDefault().displayName}.tpl",
            "$name${if (locale == null) "" else "___${locale.displayName}"}.tpl",
            "$name.tpl",
            "${name}___${Locale.getDefault().displayName}.tpl",
        )
        if (conf.aliases.containsKey(name)) {
            val name0 = conf.aliases.getValue(name)
            l.addAll(
                listOf(
                    "$name0/${if (locale == null) "main" else "___${locale.displayName}"}.tpl",
                    "$name0/${if (locale == null) "default" else "___${locale.displayName}"}.tpl",
                    "$name0/main.tpl",
                    "$name0/default.tpl",
                    "$name0/${Locale.getDefault().displayName}.tpl",
                    "$name0${if (locale == null) "" else "___${locale.displayName}"}.tpl",
                    "$name0.tpl",
                    "${name0}___${Locale.getDefault().displayName}.tpl",
                )
            )
        }
        return l.toList()
    }
}
