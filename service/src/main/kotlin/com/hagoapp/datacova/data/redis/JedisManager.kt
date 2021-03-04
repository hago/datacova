/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.redis

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolAbstract
import redis.clients.jedis.JedisSentinelPool

import java.io.Closeable

class JedisManager(config: RedisConfig) : Closeable {

    private val cfg = config
    private val internalPool: JedisPoolAbstract

    init {
        internalPool = getJedisPool()
    }

    val jedis: Jedis
        get() {
            val resource = internalPool.resource
            if (cfg.password == null) {
                resource.client.setPassword(cfg.password)
            }
            resource.select(cfg.database)
            return resource
        }


    private fun getJedisPool(): JedisPoolAbstract {
        return when (cfg.isSentinel) {
            true -> getJedisSentinelPool()
            false -> {
                val pool = JedisPool(cfg.serverConfig.host, cfg.serverConfig.port)
                cfg.password ?: pool.resource.client.setPassword(cfg.password)
                pool.resource.select(cfg.database)
                pool
            }
        }
    }

    private fun getJedisSentinelPool(): JedisPoolAbstract {
        val nodes = cfg.sentinelConfig.nodes.map { node ->
            "${node.key}:${node.value}"
        }.toSet()
        return when (cfg.password) {
            null -> JedisSentinelPool(cfg.sentinelConfig.master, nodes)
            else -> JedisSentinelPool(cfg.sentinelConfig.master, nodes, cfg.password)
        }
    }

    override fun close() {
        try {
            internalPool.resource.close()
            internalPool.close()
        } catch (e: Throwable) {
            //
        }
    }
}
