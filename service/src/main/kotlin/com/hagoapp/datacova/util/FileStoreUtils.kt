/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.config.CoVaConfig
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class FileStoreUtils private constructor() {

    companion object {
        private val instances = mutableMapOf<String, FileStoreUtils>()

        private fun getFileStore(root: String): FileStoreUtils {
            val instance = instances.compute(root) { k: String, existed: FileStoreUtils? ->
                if (existed != null) existed
                else {
                    val f = File(k)
                    if (!f.exists()) {
                        Files.createDirectory(Path.of(f.toURI()))
                    }
                    val fsu = FileStoreUtils()
                    fsu.rootPath = File(k).path
                    fsu
                }
            }
            return instance!!
        }

        fun getUploadedFileStore(): FileStoreUtils {
            return getFileStore(CoVaConfig.getConfig().fileStorage.uploadDirectory)
        }

        fun getThumbnailFileStore(): FileStoreUtils {
            return getFileStore(CoVaConfig.getConfig().fileStorage.thumbnailDirectory)
        }

        fun getTemporaryFileStore(): FileStoreUtils {
            return getFileStore(CoVaConfig.getConfig().fileStorage.tempDirectory)
        }

        fun getSshFileStore(): FileStoreUtils {
            return getFileStore(CoVaConfig.getConfig().fileStorage.sshDirectory)
        }
    }

    data class CopyTarget(
        val rootPath: String,
        val absoluteFileName: String,
        val relativeFileName: String
    )

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

    fun getRelativeFileName(fullName: String): String {
        if (!fullName.startsWith(rootPath, true)) {
            throw CoVaException("$fullName not in $rootPath")
        }
        val partial = fullName.substring(rootPath.length)
        return if (partial.startsWith("/")) partial.substring(1) else partial
    }

    fun saveFileToStore(partialFileName: String, stream: InputStream): String {
        val f = File(rootPath, partialFileName)
        val p = File(f.parent)
        if (!p.exists()) {
            Files.createDirectories(p.toPath())
        }
        val target = f.path
        FileOutputStream(target).use { fo ->
            val size = 1024 * 1024
            val buffer = ByteArray(size)
            while (true) {
                val i = stream.read(buffer, 0, size)
                if (i < 0) {
                    break
                }
                fo.write(buffer, 0, i)
            }
            return target
        }
    }

    fun readFileInStore(partialFileName: String): ByteArray? {
        val f = File(rootPath, partialFileName)
        if (!f.exists()) {
            return null
        }
        FileInputStream(f).use { fis ->
            return fis.readAllBytes()
        }
    }
}
