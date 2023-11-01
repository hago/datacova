/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util

import com.hagoapp.datacova.lib.distribute.conf.FtpConfig
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.*

class FtpClient(private val config: FtpConfig) : Closeable {

    enum class FtpMode {
        ASCII,
        BINARY
    }

    private val ftp = FTPClient()

    private var _mode = FtpMode.BINARY
    var ftpMode: FtpMode
        get() = _mode
        set(value) {
            _mode = value
            ftp.setFileType(if (_mode == FtpMode.ASCII) FTP.ASCII_FILE_TYPE else FTP.BINARY_FILE_TYPE)
        }

    init {
        ftp.connect(config.host, config.port)
        ftp.login(config.login, config.password)
    }

    fun cd(path: String): Boolean {
        return ftp.changeWorkingDirectory(path)
    }

    fun exists(path: String): Boolean {
        return ftp.changeWorkingDirectory(path)
    }

    fun pwd(): String {
        return ftp.printWorkingDirectory()
    }

    fun put(remoteFile: String, localFile: String) {
        FileInputStream(localFile).use {
            put(remoteFile, it)
        }
    }

    fun put(remoteFile: String, content: ByteArray) {
        put(remoteFile, ByteArrayInputStream(content))
    }

    fun put(remoteFile: String, stream: InputStream) {
        ftp.storeFileStream(remoteFile).use {
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val i = stream.read(buffer, 0, buffer.size)
                if (i > 0) {
                    it.write(buffer, 0, i)
                } else {
                    break
                }
            }
        }
        ftp.completePendingCommand()
    }

    fun delete(remoteFile: String): Boolean {
        return ftp.deleteFile(remoteFile)
    }

    fun get(remoteFile: String, localFile: String) {
        FileOutputStream(localFile, false).use {
            get(remoteFile, it)
        }
    }

    fun get(remoteFile: String): ByteArray {
        ByteArrayOutputStream().use {
            get(remoteFile, it)
            return it.toByteArray()
        }
    }

    fun get(remoteFile: String, outStream: OutputStream) {
        ftp.retrieveFileStream(remoteFile).use {
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val i = it.read(buffer, 0, buffer.size)
                if (i > 0) {
                    outStream.write(buffer, 0, i)
                } else {
                    break
                }
            }
        }
    }

    fun ls(): Array<String> {
        return ftp.listNames() ?: arrayOf()
    }

    fun ls(path: String): Array<String> {
        return ftp.listNames(path) ?: arrayOf()
    }

    fun list(): List<RemoteFile> {
        val pwd = pwd()
        return list(pwd)
    }

    private fun list(path: String): List<RemoteFile> {
        val ret = mutableListOf<RemoteFile>()
        ftp.listFiles(path).forEach {
            if (it != null) {
                ret.add(RemoteFile(it.name, it.isDirectory, it.isSymbolicLink, it.size, path))
            }
        }
        return ret
    }

    fun getReply(): Pair<Int, String> {
        return Pair(ftp.replyCode, ftp.replyString)
    }

    fun createDirectory(path: String) {
        ftp.mkd(path)
    }

    override fun close() {
        try {
            ftp.logout()
            ftp.disconnect()
        } catch (ex: Exception) {
            //
        }
    }
}
