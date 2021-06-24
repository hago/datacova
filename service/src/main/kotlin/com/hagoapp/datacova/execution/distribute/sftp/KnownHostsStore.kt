/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.distribute.sftp

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.distribute.sftp.HostKeyItem
import com.hagoapp.datacova.util.Utils
import com.jcraft.jsch.HostKey
import java.io.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class KnownHostsStore {

    companion object {

        private const val KNOWN_HOSTS_FILE = "known_hosts"

        private val store = KnownHostsStore()

        fun getStore(): KnownHostsStore {
            if (!store.initialized) {
                store.init()
            }
            return store
        }
    }

    private var initialized = false
    private lateinit var knownHostsFile: String

    private var hostKeys = mutableMapOf<String, HostKeyItem>()
    private val lock = ReentrantReadWriteLock()

    fun getHostKeyItem(host: String): HostKeyItem? {
        lock.read {
            return hostKeys[host]
        }
    }

    @Synchronized
    private fun init() {
        load()
        initialized = true
    }

    fun toStream(): InputStream {
        lock.read {
            val str = hostKeys.values.joinToString("\n") { "${it.host} ${it.type} ${it.key}" }
            return ByteArrayInputStream(str.toByteArray())
        }
    }

    @Synchronized
    fun updateHost(hostKey: HostKey) {
        hostKeys[hostKey.host] = HostKeyItem(hostKey.host, hostKey.type, hostKey.key)
        persist()
    }

    private fun persist() {
        FileOutputStream(knownHostsFile).use { stream ->
            val content = toStream().use { it.readAllBytes() }
            stream.write(content)
        }
    }

    private fun load(fileName: String? = null) {
        val sshDirectory = CoVaConfig.getConfig().fileStorage.sshDirectory
        knownHostsFile = fileName ?: Utils.joinPath(sshDirectory, KNOWN_HOSTS_FILE)
        if (!File(knownHostsFile).exists()) {
            File(knownHostsFile).createNewFile()
        } else {
            try {
                val content = FileInputStream(knownHostsFile).use { it.readAllBytes() }
                val newHostKeys = String(content).split("\n").mapNotNull { line ->
                    val parts = line.split(" ")
                    if (parts.size >= 3) {
                        val hk = HostKeyItem(parts[0], parts[1], parts[2])
                        Pair(hk.host, hk)
                    } else null
                }.toMap()
                lock.write { hostKeys = newHostKeys.toMutableMap() }
            } catch (ex: FileNotFoundException) {
                //
            }
        }
    }
}
