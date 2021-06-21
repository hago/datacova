/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.distribute

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.distribute.Distributor
import com.hagoapp.datacova.entity.action.distribute.TaskActionDistribute
import com.hagoapp.datacova.entity.action.distribute.conf.SFtpConfig
import com.hagoapp.datacova.execution.distribute.sftp.KnownHostsStore
import com.hagoapp.datacova.util.SFtpClient
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSchException

class SFtpDistributor(action: TaskActionDistribute) : Distributor(action) {
    private val config = distAction.configuration as SFtpConfig
    override fun distribute(source: String?) {
        var sftp: ChannelSftp? = null
        try {
            sftp = KnownHostsStore.getStore().toStream().use {
                SFtpClient(config.host, config.port, config.login, config.password, it).getClient()
            }
            sftp.cd(config.remotePath)
            if (sftp.ls("*").any {
                    (it as ChannelSftp.LsEntry).filename.compareTo(config.remoteName, true) == 0
                }) {
                if (config.isOverwriteExisted) sftp.rm(config.remoteName)
                else throw CoVaException("remote file ${config.remoteName} in ${config.remotePath} existed")
            }
            sftp.put(source, config.remoteName)
            if (!sftp.ls("*").any {
                    (it as ChannelSftp.LsEntry).filename.compareTo(config.remoteName, true) == 0
                }) {
                throw CoVaException("ftp uploaded store file ${config.remoteName} in ${config.remotePath} not found")
            }
            sftp.quit()
        } catch (ex: JSchException) {
            throw CoVaException(ex.message, ex.cause)
        } finally {
            sftp?.quit()
        }
    }
}
