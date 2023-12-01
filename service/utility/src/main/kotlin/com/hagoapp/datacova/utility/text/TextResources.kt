/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.text

import com.hagoapp.datacova.utility.CoVaException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * This class reads text localized resources from resources in jar. Name of text resource is a string containing "/".
 * Parts separated by "/" will be considered as path under resources/text folder, a file named with locale's
 * string representation or "default" for failsafe will be considered as file name, the content of this file will be
 * fetched and returned.
 * Note: This class assumes text resource files stored in text folder in resources.
 *
 * @author suncjs
 * @since 0.2
 */
object TextResources {

    private val textMap = ConcurrentHashMap<String, String>()

    private fun buildPathLocalIrrelevant(vararg paths: String): String {
        return "${createPath(*paths)}/default"
    }

    private fun buildPath(locale: Locale, vararg paths: String): String {
        return "${createPath(*paths)}/${locale}"
    }

    private fun createPath(vararg paths: String): String {
        if (paths.any { it.all { ch -> ch == '/' } || it.contains("//") }) {
            throw CoVaException("Illegal name ${paths.joinToString(",")}")
        }
        val joined =  paths.joinToString("/") {
            val s = if (it.startsWith("/")) it.substring(1) else it
            if (s.endsWith("/")) s.substring(0, s.lastIndex - 1) else s
        }
        return if (joined.startsWith("/")) joined else "/$joined"
    }

    fun getString(locale: Locale, vararg paths: String): String? {
        val path = buildPath(locale, *paths)
        return textMap.compute(path) { key, existed ->
            if (existed != null) {
                existed
            } else {
                val clz = TextResources::class.java
                val resource = clz.getResource(key) ?: clz.getResource(buildPathLocalIrrelevant(*paths))
                if (resource == null) {
                    null
                } else {
                    val bytes = resource.openConnection().getInputStream().use {
                        it.readAllBytes()
                    }
                    String(bytes, StandardCharsets.UTF_8)
                }
            }
        }
    }
}
