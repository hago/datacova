/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.util.ssh

import java.io.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read

/**
 * A class to simulate known hosts file.
 *
 * @author suncjs
 * @since 0.5
 */
interface KnownHostsStore {

    fun findHostKetItem(host: String): HostKeyItem?
    fun update(hostKetItem: HostKeyItem)
    fun toStream(): InputStream

    open class MemoryKnownHostStore : KnownHostsStore {

        protected val lock = ReentrantReadWriteLock()
        protected val itemMap = ConcurrentHashMap<String, HostKeyItem>()

        override fun findHostKetItem(host: String): HostKeyItem? {
            return itemMap[host]
        }

        override fun update(hostKetItem: HostKeyItem) {
            itemMap[hostKetItem.host] = hostKetItem
        }

        override fun toStream(): InputStream {
            lock.read {
                val str = itemMap.values.joinToString("\n") { "${it.host} ${it.type} ${it.key}" }
                return ByteArrayInputStream(str.toByteArray())
            }
        }
    }
}
