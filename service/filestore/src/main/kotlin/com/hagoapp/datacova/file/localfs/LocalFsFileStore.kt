/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.file.localfs

import com.hagoapp.datacova.file.FileInfo
import com.hagoapp.datacova.file.FileStore
import org.slf4j.LoggerFactory
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.InvalidParameterException
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.io.path.name

class LocalFsFileStore private constructor(private val rootPath: String) : FileStore {

    companion object {
        private val instances = mutableMapOf<String, LocalFsFileStore>()
        private val logger = LoggerFactory.getLogger(LocalFsFileStore::class.java)

        fun getFileStore(root: String): LocalFsFileStore {
            val key = File(root).absolutePath
            val instance = instances.compute(key) { k: String, existed: LocalFsFileStore? ->
                if (existed != null) existed
                else {
                    val f = File(k)
                    if (!f.exists()) {
                        logger.debug("create directory {}", f.absolutePath)
                        Files.createDirectory(Path.of(f.toURI()))
                    }
                    val lfs = LocalFsFileStore(File(k).absolutePath)
                    lfs
                }
            }
            return instance!!
        }
    }

    override fun putFile(src: InputStream, fileName: String, size: Long): String {
        val internal = createInternalFileName(fileName)
        val namingFileName = getNamingFile(internal)
        FileOutputStream(namingFileName).use { it.write(fileName.toByteArray(StandardCharsets.UTF_8)) }
        FileOutputStream(internal).use { out ->
            BufferedOutputStream(out)
            val bufferLength = 1024 * 1024 * 50
            val buffer = ByteArray(bufferLength)
            while (true) {
                val i = src.read(buffer, 0, bufferLength)
                if (i < 0) {
                    break
                }
                out.write(buffer, 0, i)
            }
        }
        return File(internal).toPath().fileName.name
    }

    /**
     * Create the actual internal name that file will be saved as.
     * Subdirectory will be created per day as yyyy/MM/dd,
     * Filename is a combination of 8 digits date and the md5 hash of concat original name and epoch millisecond at
     * that moment.
     */
    private fun createInternalFileName(originalName: String): String {
        val dt = LocalDateTime.now(ZoneId.of("UTC"))
        val subDirectory = dt.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
        val pf = File(rootPath, subDirectory)
        if (!pf.exists()) {
            Files.createDirectories(pf.toPath())
        }
        val alg = MessageDigest.getInstance("md5")
        val hashName = alg.digest("$originalName.${Instant.now().toEpochMilli()}".toByteArray(StandardCharsets.UTF_8))
            .joinToString("", transform = { aByte -> String.format("%02x", aByte) })
        val internalName = subDirectory.replace("/", "") + hashName
        return File(pf, internalName).absolutePath
    }

    private fun getNamingFile(fileName: String): String {
        return "$fileName.name"
    }

    private fun parseId(id: String): String {
        if (id.length != 40) {
            throw InvalidParameterException("Not found")
        }
        val prefix = id.substring(0, 8)
        if (prefix.any { !it.isDigit() }) {
            throw InvalidParameterException("Not found")
        }
        return "${prefix.substring(0, 4)}/${prefix.substring(4, 6)}/${prefix.substring(6, 8)}/${id}"
    }

    override fun getFile(id: String): InputStream {
        val fn = parseId(id)
        val f = File(rootPath, fn)
        if (!f.exists()) {
            throw FileNotFoundException("file $id not found")
        }
        return FileInputStream(f)
    }

    override fun getFileInfo(id: String): FileInfo {
        val fn = getNamingFile(parseId(id))
        if (!File(rootPath, fn).exists()) {
            throw FileNotFoundException("naming file $fn for $id not found")
        }
        val originalFileName = FileInputStream(fn).use { it.readAllBytes().toString(StandardCharsets.UTF_8) }
        val fn1 = File(parseId(id))
        if (!fn1.exists()) {
            throw FileNotFoundException("file $id not found")
        }
        val size = fn1.length()
        return FileInfo(originalFileName, id, size)
    }

    override fun delete(id: String): Boolean {
        val file = File(rootPath, parseId(id))
        if (file.exists()) {
            if (!file.delete()) {
                logger.error("Failed deletion of file: {}", file.absoluteFile)
                return false
            }
        }
        val file1 = File(rootPath, "${parseId(id)}.name")
        if (file1.exists()) {
            if (!file1.delete()) {
                logger.error("Failed deletion of file: {}", file1.absoluteFile)
                return false
            }
        }
        return true
    }
}
