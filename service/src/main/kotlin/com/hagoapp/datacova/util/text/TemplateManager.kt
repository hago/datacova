/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.text

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.config.TemplateConfig
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*

abstract class TemplateManager private constructor(protected val conf: TemplateConfig) {
    companion object {

        private val managers = mutableMapOf<TemplateConfig, TemplateManager>()

        @JvmStatic
        fun getManager(conf: TemplateConfig): TemplateManager {
            val tm = managers.compute(conf) { newConf, existed ->
                existed ?: createManager(newConf)
            }
            return tm!!
        }

        @JvmStatic
        fun getResourcedTemplateManager(): TemplateManager {
            val resourceCfg = TemplateConfig()
            resourceCfg.useResource = true
            return getManager(resourceCfg)
        }

        private fun createManager(conf: TemplateConfig): TemplateManager {
            return when {
                conf.useResource -> ResourceTemplateManager(conf)
                else -> FileTemplateManager(conf)
            }
        }

        class FileTemplateManager(conf: TemplateConfig) : TemplateManager(conf) {

            init {
                config.setDirectoryForTemplateLoading(File(conf.directory))
            }

            private fun findTemplateFile(name: String, locale: Locale): String? {
                for (fileName in createPossibleTemplateFileNames(name, locale)) {
                    if (File(conf.directory, fileName).exists()) {
                        return fileName
                    }
                }
                return null
            }

            override fun getTemplate(name: String, locale: Locale): Template? {
                val actualName = findTemplateFile(name, locale) ?: return null
                return config.getTemplate(actualName, locale)
            }
        }
    }

    class ResourceTemplateManager(conf: TemplateConfig) : TemplateManager(conf) {

        private val templates = mutableMapOf<String, Template>()

        init {
            config.setClassForTemplateLoading(ResourceTemplateManager::class.java, "/templates")
        }

        override fun getTemplate(name: String, locale: Locale): Template? {
            val k = "${name}___$locale"
            return templates.compute(k) { _, existed ->
                if (existed != null) {
                    existed
                } else {
                    for (n in createPossibleTemplateFileNames(name, locale)) {
                        try {
                            println("attempt $n")
                            return@compute config.getTemplate(n, locale)
                        } catch (ignored: Exception) {
                            println(ignored)
                        }
                    }
                    null
                }
            }
        }
    }

    protected val config: Configuration

    init {
        try {
            config = Configuration(Configuration.VERSION_2_3_28)
            config.defaultEncoding = "utf-8"
            config.templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
            config.logTemplateExceptions = false
            config.wrapUncheckedExceptions = true
        } catch (ex: IOException) {
            throw CoVaException("template init error", ex)
        }

    }

    fun getTemplate(name: String): Template? {
        return getTemplate(name, Locale.getDefault())
    }

    abstract fun getTemplate(name: String, locale: Locale): Template?

    protected fun createPossibleTemplateFileNames(name: String, locale: Locale?): List<String> {
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
        if (conf.aliases.containsKey(name)) {
            val name0 = conf.aliases.getValue(name)
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
