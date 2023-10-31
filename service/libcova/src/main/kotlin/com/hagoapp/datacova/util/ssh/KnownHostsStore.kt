/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.util.ssh

import com.hagoapp.datacova.file.FileStore
import com.jcraft.jsch.HostKey
import java.io.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * A class to simulate known hosts file.
 *
 * @author suncjs
 * @since 0.5
 */
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
    private lateinit var actualStore: FileStore

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
        hostKeys[hostKey.host] =
            HostKeyItem(hostKey.host, hostKey.type, hostKey.key)
        persist()
    }

    private fun persist() {
        FileOutputStream(knownHostsFile).use { stream ->
            val content = toStream().use { it.readAllBytes() }
            stream.write(content)
        }
    }

    private fun load(fileName: String? = null) {
        //init actualStore
        knownHostsFile = fileName ?: KNOWN_HOSTS_FILE
        if (!actualStore.exists(knownHostsFile)) {
            ByteArrayInputStream(ByteArray(0)).use {
                actualStore.putFile(it, knownHostsFile, 0)
            }
        } else {
            val content = actualStore.getFile(knownHostsFile).use { it.readAllBytes() }
            val newHostKeys = String(content).split("\n").mapNotNull { line ->
                val parts = line.split(" ")
                if (parts.size >= 3) {
                    val hk = HostKeyItem(parts[0], parts[1], parts[2])
                    Pair(hk.host, hk)
                } else null
            }.toMap()
            lock.write { hostKeys = newHostKeys.toMutableMap() }
        }
    }
}
