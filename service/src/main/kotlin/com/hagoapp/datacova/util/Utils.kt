/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util

import com.hagoapp.datacova.CoVaException
import java.io.File
import java.math.BigInteger
import java.nio.file.FileSystems
import java.nio.file.Files
import java.security.MessageDigest
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random


class Utils {
    companion object {
        @JvmStatic
        fun joinPath(vararg paths: String): String {
            var f = File(paths[0])
            paths.takeLast(paths.size - 1).forEach { path ->
                f = File(f, path)
            }
            return f.path
        }

        @JvmStatic
        fun getCurrentPath(): String {
            return File(".").canonicalPath
        }

        @JvmStatic
        fun generateUploadedFileName(original: String): String {
            val f = File(original)
            val path = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val md5 = MessageDigest.getInstance("MD5")
            md5.update(Random(Instant.now().epochSecond).nextBytes(1))
            val name = BigInteger(1, md5.digest()).toString(16).toUpperCase()
            return "$path/$name.${f.extension}"
        }

        @JvmStatic
        fun md5Digest(obj: Any): String {
            val md5 = MessageDigest.getInstance("MD5")
            md5.update("$obj".toByteArray())
            return BigInteger(1, md5.digest()).toString(16)
        }

        @JvmStatic
        fun sha1Digest(obj: Any): String {
            val sha1 = MessageDigest.getInstance("SHA-1")
            sha1.update("$obj".toByteArray())
            return BigInteger(1, sha1.digest()).toString(16)
        }

        @JvmStatic
        fun sha256Digest(obj: Any): String {
            val sha1 = MessageDigest.getInstance("SHA-256")
            sha1.update("$obj".toByteArray())
            return BigInteger(1, sha1.digest()).toString(16)
        }

        @JvmStatic
        fun getRandomNumber(): Long {
            return Instant.now().toEpochMilli() + Random(Instant.now().toEpochMilli()).nextLong()
        }

        @JvmStatic
        fun genRandomString(
            length: Int,
            candidates: String?
        ): String {
            val chars = candidates ?: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return when {
                length <= 0 -> throw Exception("Length of string to be generated must be positive")
                else -> {
                    val rand = Random(Instant.now().toEpochMilli())
                    val sb = StringBuilder()
                    for (i in 0 until length) {
                        sb.append(chars[rand.nextInt(0, chars.length)])
                    }
                    sb.toString()
                }
            }
        }

        @JvmStatic
        fun parseFileName(name: String): FileNameParts {
            val pathParts = name.split(File.separator)
            val path = if (pathParts.size == 1) ""
            else pathParts.take(pathParts.size - 1).joinToString(File.separator)
            val fnParts = pathParts.last().split('.')
            val fn = if (fnParts.size > 1) fnParts.take(fnParts.size - 1).joinToString(".") else fnParts[0]
            val ext = if (fnParts.size > 1) fnParts.last() else ""
            return FileNameParts(path, fn, ext)
        }

        data class FileNameParts(
            val path: String,
            val name: String,
            val ext: String
        ) {
            fun nameWithExt(): String {
                return "$name${if (ext.isBlank()) "" else ".$ext"}"
            }
        }

        @JvmStatic
        fun getSystemTemporaryDirectory(): String {
            val t1 = System.getProperty("java.io.tmpdir")
            if (t1 != null) {
                return t1
            }
            val temp = Files.createTempFile("", ".tmp")
            val absolutePath: String = temp.toString()
            val separator = FileSystems.getDefault().separator
            return absolutePath.substring(0, absolutePath.lastIndexOf(separator))
        }

        @JvmStatic
        fun splitPath(path: String, delimiter: String = File.pathSeparator): List<String> {
            val ret = mutableListOf<String>()
            var start = 0
            if (path.startsWith(delimiter)) {
                val i = path.indexOf('/', 1)
                when (i) {
                    -1 -> return listOf(path)
                    1 -> throw CoVaException("Invalid path $path")
                    else -> {
                        start = 1
                        ret.add(path.substring(0, i))
                    }
                }
            }
            ret.addAll(path.substring(start).split("/").filter { it.isNotBlank() })
            return ret
        }
    }
}
