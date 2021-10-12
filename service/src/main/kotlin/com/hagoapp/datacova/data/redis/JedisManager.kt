/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.redis

import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisSentinelPool
import redis.clients.jedis.util.Pool

class JedisManager(private val config: RedisConfig) {

    companion object {
        private var defaultPool: Pool<Jedis>? = null
        private val internalPools = mutableMapOf<RedisConfig, Pool<Jedis>>()

        fun terminate() {
            internalPools.forEach { (_, pool) ->
                try {
                    pool.close()
                } catch (e: Throwable) {
                    //
                }
            }
            internalPools.clear()
        }

        @JvmStatic
        fun getJedis(): Jedis? {
            return defaultPool?.resource
        }

        @JvmStatic
        fun getJedis(config: RedisConfig): Jedis {
            return internalPools.compute(config) { k, existed ->
                if (existed == null) {
                    JedisManager(k)
                    internalPools.getValue(k)
                } else {
                    existed
                }
            }!!.resource
        }
    }

    init {
        internalPools.compute(config) { _, existed ->
            if (existed == null) {
                val p = getJedisPool()
                if (defaultPool == null) {
                    defaultPool = p
                }
                p
            } else {
                existed
            }
        }
    }

    private fun getJedisPool(): Pool<Jedis> {
        return when (config.isSentinel) {
            true -> getJedisSentinelPool()
            false -> {
                val pool = JedisPool(config.serverConfig.host, config.serverConfig.port)
                config.password ?: pool.resource.client.auth(config.password)
                pool
            }
        }
    }

    private fun getJedisSentinelPool(): Pool<Jedis> {
        val nodes = config.sentinelConfig.nodes.map { node ->
            "${node.key}:${node.value}"
        }.toSet()
        return when (config.password) {
            null -> JedisSentinelPool(config.sentinelConfig.master, nodes)
            else -> JedisSentinelPool(config.sentinelConfig.master, nodes, config.password)
        }
    }

}
