/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.redis

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.ShutDownManager
import com.hagoapp.datacova.ShutDownWatcher
import com.hagoapp.datacova.config.CoVaConfig
import org.slf4j.Logger
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.JedisSentinelPool
import redis.clients.jedis.util.Pool
import java.time.Duration
import java.time.temporal.ChronoUnit

class JedisManager private constructor() {

    private class PortalRedisCloser : ShutDownWatcher {

        override fun shutdown(): Boolean {
            return try {
                pool.close()
                true
            } catch (e: Exception) {
                logger.error("Closing internal redis pool error: {}", e.message)
                false
            }
        }

        override fun getName(): String {
            return "Jedis Manager"
        }
    }

    companion object {
        private val logger: Logger = CoVaLogger.getLogger()
        private var pool: Pool<Jedis>
        private var dbIndex = 0

        init {
            val config = CoVaConfig.getConfig().redis
            pool = if (config.isSentinel) createSentinelPool(config) else createServerPool(config)
            dbIndex = config.database
            val closer = PortalRedisCloser()
            ShutDownManager.watch(closer)
        }

        private fun createSentinelPool(config: RedisConfig): Pool<Jedis> {
            val sentinelConfig = config.sentinelConfig
            val nodeSet = sentinelConfig.nodes.map { (host, port) ->
                java.lang.String.format("%s:%d", host, port)
            }.toSet()
            return if (config.password == null) JedisSentinelPool(sentinelConfig.master, nodeSet)
            else JedisSentinelPool(sentinelConfig.master, nodeSet, config.password)
        }

        private fun createServerPool(config: RedisConfig): Pool<Jedis> {
            val jedisPoolConfig = JedisPoolConfig()
            jedisPoolConfig.maxTotal = -1
            jedisPoolConfig.maxIdle = 80
            jedisPoolConfig.maxWaitMillis = 30000
            jedisPoolConfig.timeBetweenEvictionRuns = Duration.of(1000, ChronoUnit.MILLIS)
            jedisPoolConfig.softMinEvictableIdleTime = Duration.of(1000, ChronoUnit.MILLIS)
            jedisPoolConfig.lifo = true
            jedisPoolConfig.numTestsPerEvictionRun = 50
            jedisPoolConfig.testOnBorrow = false
            jedisPoolConfig.testOnCreate = false
            jedisPoolConfig.minIdle = 10
            val serverConfig = config.serverConfig
            return JedisPool(jedisPoolConfig, serverConfig.host, serverConfig.port, 10000, config.password)
        }

        /**
         * Fetch a jedis instance, which should be closed explicitly.
         *
         * @return jedis instance
         */
        fun getJedis(): Jedis {
            val jedis = pool.resource
            jedis.select(dbIndex)
            return jedis
        }
    }
}
