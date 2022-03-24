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
import com.hagoapp.datacova.distribute.conf.FtpConfig
import com.hagoapp.datacova.util.FtpClient
import com.hagoapp.datacova.util.Utils

class FtpDistributor() : Distributor() {
    private lateinit var config: FtpConfig
    override fun init(action: TaskActionDistribute?) {
        super.init(action)
        if (action == null) {
            throw CoVaException("null distribute action!")
        }
        config = action.configuration as FtpConfig
    }

    override fun distribute(source: String) {
        FtpClient(config).use { ftp ->
            ftp.ftpMode = if (config.isBinaryTransport) FtpClient.FtpMode.BINARY else FtpClient.FtpMode.ASCII
            createDirectoryIfNecessary(ftp, config.remotePath)
            ftp.cd(config.remotePath)
            val rName = if (config.remoteName != null) config.remoteName else config.targetFileName
            if (ftp.ls(config.remotePath).any { file -> file.compareTo(rName) == 0 }) {
                if (config.isOverwriteExisted) {
                    ftp.delete(rName)
                } else {
                    throw CoVaException("remote file $rName in ${config.remotePath} existed")
                }
            }
            ftp.put(rName, source)
            if (!ftp.ls(config.remotePath).any { file ->
                    //println(file)
                    file.compareTo(rName) == 0
                }) {
                throw CoVaException("ftp uploaded store file $rName in ${config.remotePath} not found")
            }
        }
    }

    private fun createDirectoryIfNecessary(ftp: FtpClient, path: String) {
        if (ftp.exists(path)) {
            return
        }
        val parts = Utils.splitPath(path, "/")
        for (sub in parts) {
            if (!ftp.exists(sub)) {
                ftp.createDirectory(sub)
                logger.debug("mkdir $sub")
            }
            ftp.cd(sub)
            logger.debug("cd $sub")
        }
    }

    override fun supportedDistributionType(): String {
        return FtpConfig.DISTRIBUTION_TYPE_FTP
    }
}
