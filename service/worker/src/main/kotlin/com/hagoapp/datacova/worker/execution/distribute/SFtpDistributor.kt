/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.distribute

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.distribute.Distributor
import com.hagoapp.datacova.distribute.TaskActionDistribute
import com.hagoapp.datacova.distribute.conf.SFtpConfig
import com.hagoapp.datacova.execution.distribute.sftp.KnownHostsStore
import com.hagoapp.datacova.lib.util.SFtpClient
import com.hagoapp.datacova.util.Utils
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.SftpException

class SFtpDistributor() : Distributor() {
    private lateinit var config: SFtpConfig
    override fun init(action: TaskActionDistribute?) {
        super.init(action)
        if (action == null) {
            throw CoVaException("null distribute action!")
        }
        config = action.configuration as SFtpConfig
    }

    override fun distribute(source: String?) {
        SFtpClient(config, KnownHostsStore.getStore()).use {
            try {
                val sftp = it.getClient()
                val actualPath = normalizePath(sftp, config.remotePath)
                createDirectoryIfNecessary(sftp, actualPath)
                val rName = if ((config.remoteName != null) && config.remoteName.isNotBlank()) config.remoteName
                else config.targetFileName
                sftp.cd(actualPath)
                if (sftp.ls("*").any { entry ->
                        (entry as ChannelSftp.LsEntry).filename.compareTo(rName, true) == 0
                    }) {
                    if (config.isOverwriteExisted) sftp.rm(rName)
                    else throw CoVaException("remote file $rName in ${config.remotePath} existed")
                }
                sftp.put(source, rName)
                if (!sftp.ls("*").any { entry ->
                        (entry as ChannelSftp.LsEntry).filename.compareTo(rName, true) == 0
                    }) {
                    throw CoVaException("ftp uploaded store file $rName in ${config.remotePath} not found")
                }
            } catch (ex: JSchException) {
                throw CoVaException(ex.message, ex.cause)
            }
        }
    }

    private fun normalizePath(sftp: ChannelSftp, path: String): String {
        return if (path.startsWith("~")) {
            val home = sftp.home
            val rest = path.substring(1)
            "$home${if (rest == "/") "" else rest}"
        } else {
            path
        }
    }

    private fun createDirectoryIfNecessary(sftp: ChannelSftp, path: String) {
        try {
            sftp.cd(path)
        } catch (e: SftpException) {
            createDirectory(sftp, path)
        }
    }

    private fun createDirectory(sftp: ChannelSftp, path: String) {
        val parts = Utils.splitPath(path, "/")
        for (p in parts) {
            try {
                sftp.cd(p)
            } catch (e: SftpException) {
                sftp.mkdir(p)
                sftp.cd(p)
            }
        }
    }

    override fun supportedDistributionType(): String {
        return SFtpConfig.DISTRIBUTE_TYPE_SFTP
    }
}
