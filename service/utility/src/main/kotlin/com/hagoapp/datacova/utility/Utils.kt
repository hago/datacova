/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.math.BigInteger
import java.nio.file.FileSystems
import java.nio.file.Files
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Utils {
    companion object {
        private val random = SecureRandom(toBytes(Instant.now().toEpochMilli()))
        private const val DIGEST_ALGORITHM = "SHA-256"

        @JvmStatic
        fun toBytes(input: Long): ByteArray {
            return ByteArray(8) {
                input.shr(it).and(0x000000f).toByte()
            }
        }

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
            val md5 = MessageDigest.getInstance(DIGEST_ALGORITHM)
            md5.update(path.toByteArray())
            val bytes = ByteArray(1)
            random.nextBytes(bytes)
            md5.update(bytes)
            val name = BigInteger(1, md5.digest()).toString(16).uppercase()
            return "$path/$name.${f.extension}"
        }

        @JvmStatic
        fun md5Digest(obj: Any): String {
            val md5 = MessageDigest.getInstance(DIGEST_ALGORITHM)
            md5.update("$obj".toByteArray())
            return BigInteger(1, md5.digest()).toString(16)
        }

        @JvmStatic
        fun sha256Digest(obj: Any): String {
            val sha1 = MessageDigest.getInstance(DIGEST_ALGORITHM)
            sha1.update("$obj".toByteArray())
            return BigInteger(1, sha1.digest()).toString(16)
        }

        @JvmStatic
        fun getRandomNumber(): Long {
            return Instant.now().toEpochMilli() + random.nextLong()
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
                    val sb = StringBuilder()
                    for (i in 0 until length) {
                        sb.append(chars[random.nextInt(chars.length)])
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
            if (Regex.fromLiteral("[$delimiter]{2,}").containsMatchIn(path)) {
                throw CoVaException("not a valid path: $path")
            }
            val ret = mutableListOf<String>()
            val path0 = if (path.startsWith(delimiter)) {
                ret.add(delimiter)
                path.substring(1)
            } else {
                path
            }
            ret.addAll(path0.split(delimiter))
            return ret
        }

        @JvmStatic
        @Throws(IOException::class)
        fun copyStream(fis: InputStream, fos: OutputStream, bufferSize: Int = 1024 * 1024 * 10) {
            val buffer = ByteArray(bufferSize)
            while (true) {
                val i = fis.read(buffer, 0, buffer.size)
                if (i < 0) {
                    break
                }
                fos.write(buffer, 0, i)
            }
        }
    }
}
