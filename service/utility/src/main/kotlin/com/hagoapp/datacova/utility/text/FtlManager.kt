/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.utility.text

import com.hagoapp.datacova.utility.CoVaException
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Manager class for freemarker templates. It can search template files from resources or local file directories, and
 * try to find the one with right locale.
 * Priorities:
 * <literal>
 * [template name]/main_[locale name].ftl
 * [template name]/default_[locale name].ftl
 * [template name]/[locale name].ftl
 * [template name]__main_[locale name].ftl
 * [template name]/main.ftl
 * [template name]/default.ftl
 * [template name]/[system locale name].ftl
 * [template name]_[locale name].ftl
 * [template name].ftl
 * </literal>
 *
 * @property conf
 * @constructor Create empty Template manager
 */
abstract class FtlManager private constructor(protected val conf: FtlConfig) {
    companion object {

        private val managers = mutableMapOf<FtlConfig, FtlManager>()
        private val logger = LoggerFactory.getLogger(FtlManager::class.java)

        @JvmStatic
        fun getManager(conf: FtlConfig): FtlManager {
            val tm = managers.compute(conf) { newConf, existed ->
                existed ?: createManager(newConf)
            }
            return tm!!
        }

        @JvmStatic
        fun getResourcedTemplateManager(): FtlManager {
            val resourceCfg = FtlConfig()
            resourceCfg.isUseResource = true
            return getManager(resourceCfg)
        }

        private fun createManager(conf: FtlConfig): FtlManager {
            return when {
                conf.isUseResource -> ResourceFtlManager(conf)
                else -> FileFtlManager(conf)
            }
        }
    }

    class FileFtlManager(conf: FtlConfig) : FtlManager(conf) {

        private val logger = LoggerFactory.getLogger(FileFtlManager::class.java)

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

    class ResourceFtlManager(conf: FtlConfig) : FtlManager(conf) {

        private val templates = mutableMapOf<String, Template>()
        private val logger = LoggerFactory.getLogger(ResourceFtlManager::class.java)

        init {
            config.setClassForTemplateLoading(ResourceFtlManager::class.java, "/")
        }

        override fun getTemplate(name: String, locale: Locale): Template? {
            val k = "${name}___$locale"
            return templates.compute(k) { _, existed ->
                if (existed != null) {
                    existed
                } else {
                    for (n in createPossibleTemplateFileNames(name, locale)) {
                        try {
                            logger.debug("try to find template names with local: {}", n)
                            val t = config.getTemplate(n, locale)
                            logger.info("template file {} found for locale {}", n, locale)
                            return@compute t
                        } catch (ignored: Exception) {
                            logger.debug("named file {} not found, skip and try next", n)
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
        val l = mutableListOf<String>()
        l.addAll(createSortedTemplatesSearchList(name, locale))
        if (conf.aliases.containsKey(name)) {
            val name0 = conf.aliases.getValue(name)
            l.addAll(createSortedTemplatesSearchList(name0, locale))
        }
        return l.toList()
    }

    private fun createSortedTemplatesSearchList(name: String, locale: Locale?): List<String> {
        val l = mutableListOf<String>()
        if (locale != null) {
            l.addAll(
                listOf(
                    "$name/main_$locale.ftl",
                    "$name/default_$locale.ftl",
                    "${name}/$locale.ftl",
                    "${name}__$locale.ftl",
                )
            )
        }
        l.addAll(
            listOf(
                "$name/main.ftl",
                "$name/default.ftl",
                "$name/${Locale.getDefault()}.ftl",
                "${name}__${Locale.getDefault()}.ftl",
                "$name.ftl",
            )
        )
        return l
    }
}
