/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.executor.distribute

import com.hagoapp.datacova.utility.CoVaException
import com.hagoapp.datacova.lib.distribute.TaskActionDistribute
import com.hagoapp.datacova.lib.distribute.conf.SFtpConfig
import com.hagoapp.datacova.lib.util.SFtpClient
import com.hagoapp.datacova.lib.util.ssh.KnownHostsStore
import java.io.InputStream

class SFtpDistributor() : Distributor() {
    private lateinit var config: SFtpConfig
    override fun init(action: TaskActionDistribute?) {
        super.init(action)
        if (action == null) {
            throw CoVaException("null distribute action!")
        }
        config = action.configuration as SFtpConfig
    }

    override fun distribute(src: InputStream) {
        SFtpClient(config, KnownHostsStore.MemoryKnownHostStore()).use {
            try {
                if (!it.cd(config.remotePath)) {
                    throw CoVaException("remote path ${config.remotePath} doesn't exist")
                }
                val actualPath = normalizePath(it, config.remotePath)
                it.mkdir(actualPath)
                val rName = if ((config.remoteName != null) && config.remoteName.isNotBlank()) config.remoteName
                else config.targetFileName
                if (it.list(actualPath).any { item -> item.name.compareTo(rName) == 0 }) {
                    if (config.isOverwriteExisted) it.rm(rName)
                    else throw CoVaException("remote file $rName in ${config.remotePath} existed")
                }
                it.put(src, rName)
            } catch (ex: Exception) {
                throw CoVaException(ex.message, ex.cause)
            }
        }
    }

    private fun normalizePath(sftp: SFtpClient, path: String): String {
        return if (path.startsWith("~")) {
            val home = sftp.home()
            val rest = path.substring(1)
            "$home${if (rest == "/") "" else rest}"
        } else {
            path
        }
    }

    override fun supportedDistributionType(): String {
        return SFtpConfig.DISTRIBUTE_TYPE_SFTP
    }
}
