/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.filestore

import com.hagoapp.datacova.file.FsConfig
import com.hagoapp.datacova.file.FsScheme
import com.hagoapp.datacova.utility.filestore.RedisFsConfig.Companion.REDIS_FILE_STORE
import com.hagoapp.datacova.utility.redis.RedisConfig
import com.hagoapp.datacova.utility.redis.RedisSentinelConfig
import com.hagoapp.datacova.utility.redis.RedisServerConfig

/**
 * Redis file store config.Connection string schema: <i>redisfs:/[HOST_INFO]/[DB]/[PASSWORD]</i>
 * Example:
 * redisfs:myredisfs/127.0.01:6379
 * redisfs:myredisfs/127.0.01:6379/1
 * redisfs:myredisfs/127.0.01:6379/1/!!abc123
 * redisfs:myredisfs/192.168.1.10:2637#192.168.1.11:26379#192.168.1.12:26379/0/$%^&*(
 *
 * @constructor Create empty Redis fs config
 */
@FsScheme(name = REDIS_FILE_STORE)
class RedisFsConfig : FsConfig() {
    companion object {
        const val REDIS_FILE_STORE = "redisFs"
    }

    var name: String = REDIS_FILE_STORE
    var config: RedisConfig = RedisConfig()

    override fun serialize(): String {
        return REDIS_FILE_STORE
    }

    override fun loadConnectionString(input: String) {
        val i = input.indexOf(':')
        if (i < 0) {
            throw UnsupportedOperationException("Not an FsConfig connection string")
        }
        val schema = input.substring(0)
        if (REDIS_FILE_STORE.compareTo(schema, true) == 0) {
            throw UnsupportedOperationException("Not a RedisFsConfig connection string")
        }
        val conn = input.substring(i + 1)
        val parts = conn.split('/', limit = 4)
        if (parts.size < 2) {
            throw UnsupportedOperationException("Not a RedisFsConfig connection string")
        }
        name = parts[0]
        val x = parseHosts(parts[1])
        val db = if (parts.size > 2) parts[2].toInt() else 0
        val password = if (parts.size > 3) parts[3] else null
        config = RedisConfig(
            x.second != null,
            x.first ?: RedisServerConfig(),
            x.second ?: RedisSentinelConfig(),
            password,
            db
        )
    }

    private fun parseHosts(hosts: String): Pair<RedisServerConfig?, RedisSentinelConfig?> {
        val hostParts = hosts.split("#")
        return if (hostParts.size > 1) Pair(null, parseSentinel(hostParts))
        else Pair(parseServer(hosts), null)
    }

    private fun parseServer(input: String, defaultPort: Int = 6379): RedisServerConfig {
        val parts = input.split(":")
        return when {
            parts.size > 2 -> throw UnsupportedOperationException("Invalid host: $input")
            parts.size == 1 -> RedisServerConfig(parts[0], defaultPort)
            else -> RedisServerConfig(
                parts[0],
                parts[1].toIntOrNull() ?: throw UnsupportedOperationException("Invalid host: $input")
            )
        }
    }

    private fun parseSentinel(hosts: List<String>): RedisSentinelConfig {
        val nodes = hosts.map { parseServer(it, 26379) }
        return RedisSentinelConfig(
            nodes.associate { Pair(it.host, it.port) },
            nodes[0].host
        )
    }
}
