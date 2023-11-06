/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.redis

data class RedisConfig(
    val isSentinel: Boolean = false,
    val serverConfig: RedisServerConfig = RedisServerConfig(),
    val sentinelConfig: RedisSentinelConfig = RedisSentinelConfig(),
    val password: String? = null,
    val database: Int = 0
) {
    override fun toString(): String {
        return "RedisConfig(isSentinel=$isSentinel, serverConfig=$serverConfig, sentinelConfig=$sentinelConfig, password=$password, database=$database)"
    }
}

data class RedisServerConfig(
    val host: String = "127.0.0.1",
    val port: Int = 6379
) {
    override fun toString(): String {
        return "RedisServerConfig(host='$host', port=$port)"
    }
}

data class RedisSentinelConfig(
    val nodes: Map<String, Int> = mapOf(),
    val master: String = ""
) {
    override fun toString(): String {
        return "RedisSentinelConfig(nodes=$nodes, master='$master')"
    }
}

fun interface RedisConfigProvider {
    fun getConfig(): RedisConfig
}
