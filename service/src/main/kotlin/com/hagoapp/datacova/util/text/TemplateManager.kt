/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.text

import com.hagoapp.datacova.CoVaException
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import java.io.File
import java.io.IOException
import java.util.*

class TemplateManager private constructor(private val directory: File, private val aliases: Map<String, String>) {
    companion object {

        private val managers = mutableMapOf<File, TemplateManager>()

        @JvmStatic
        fun getManager(directory: File, aliases: Map<String, String>): TemplateManager {
            val tm = managers.compute(directory) { _, existed ->
                existed ?: TemplateManager(directory, aliases)
            }
            return tm!!
        }

        @JvmStatic
        fun getResourceTemplateManager(): TemplateManager {
            val clz = TextResourceManager::class.java
            val resource = clz.getResource("/templates") ?: throw CoVaException("resource templates not found")
            return getManager(File(resource.toURI()), mapOf())
        }
    }

    private val config: Configuration

    init {
        try {
            config = Configuration(Configuration.VERSION_2_3_28)
            config.defaultEncoding = "utf-8"
            config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
            config.logTemplateExceptions = false
            config.wrapUncheckedExceptions = true
            config.setDirectoryForTemplateLoading(directory)
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
            if (File(directory, fileName).exists()) {
                return fileName
            }
        }
        return null
    }

    private fun createPossibleTemplateFileNames(name: String, locale: Locale?): List<String> {
        val l = mutableListOf(
            "$name/${if (locale == null) "main" else "___${locale}"}.ftl",
            "$name/${if (locale == null) "default" else "___${locale}"}.ftl",
            "$name/main.ftl",
            "$name/default.ftl",
            "$name/${Locale.getDefault()}.ftl",
            "$name${if (locale == null) "" else "___${locale}"}.ftl",
            "$name.ftl",
            "${name}___${Locale.getDefault()}.ftl",
        )
        if (aliases.containsKey(name)) {
            val name0 = aliases.getValue(name)
            l.addAll(
                listOf(
                    "$name0/${if (locale == null) "main" else "___${locale}"}.ftl",
                    "$name0/${if (locale == null) "default" else "___${locale}"}.ftl",
                    "$name0/main.ftl",
                    "$name0/default.ftl",
                    "$name0/${Locale.getDefault()}.ftl",
                    "$name0${if (locale == null) "" else "___${locale}"}.ftl",
                    "$name0.ftl",
                    "${name0}___${Locale.getDefault()}.ftl",
                )
            )
        }
        return l.toList()
    }
}
