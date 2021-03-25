/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util

import com.hagoapp.datacova.config.CoVaConfig
import java.io.File
import java.nio.charset.Charset
import java.nio.file.Files
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class FileStoreUtils private constructor() {

    companion object {
        private val instance: FileStoreUtils = FileStoreUtils()

        fun getFileStore(): FileStoreUtils {
            if (!instance.initialized) {
                instance.initialized = true
                val cfg = CoVaConfig.getConfig()
                instance.rootPath = cfg.task.fileStoreRoot
            }
            return instance
        }
    }

    data class CopyTarget(
        val rootPath: String,
        val absoluteFileName: String,
        val relativeFileName: String
    )

    private var initialized = false
    private lateinit var rootPath: String

    /**
     * @param src       full path of source file
     * @return a class contains path info of target
     */
    fun copyFileToStore(src: String): CopyTarget {
        val dt = LocalDateTime.now(ZoneId.of("UTC"))
        val subDirectory = dt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val pf = File(rootPath, subDirectory)
        if (!pf.exists()) {
            Files.createDirectories(pf.toPath())
        }
        val alg = MessageDigest.getInstance("md5")
        val fileName = alg.digest(
            "$src - ${Random(dt.toInstant(ZoneOffset.UTC).toEpochMilli())}".toByteArray(Charset.forName("UTF-8"))
        ).joinToString("", transform = { aByte -> String.format("%02x", aByte) })
        val target = File(pf, fileName).toPath()
        Files.copy(File(src).toPath(), target)
        return CopyTarget(rootPath, target.toAbsolutePath().toString(), Utils.joinPath(subDirectory, fileName))
    }

    fun isFileExists(partialFileName: String): Boolean {
        return File(getFullFileName(partialFileName)).exists()
    }

    fun getFullFileName(partialFileName: String): String {
        return Utils.joinPath(rootPath, partialFileName)
    }
}
