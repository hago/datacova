/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.redis

import com.hagoapp.datacova.ShutDownManager
import com.hagoapp.datacova.ShutDownWatcher
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.CoVaRedisConfigProvider
import org.slf4j.LoggerFactory
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
            val results = pools.map { (config, pool) ->
                try {
                    pool.first.close()
                    true
                } catch (ignored: Throwable) {
                    logger.error("Closing internal redis pool {} error: {}", config, ignored.message)
                    false
                }
            }
            return results.all { it }
        }

        override fun getName(): String {
            return "Jedis Manager"
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JedisManager::class.java)
        private val pools = mutableMapOf<RedisConfig, Pair<Pool<Jedis>, Int>>()
        private val defaultConfig = CoVaConfig.getConfig().redis

        interface ConfigProvider {
            fun getConfig(): RedisConfig
        }

        init {
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
            jedisPoolConfig.setMaxWait(Duration.of(30000, ChronoUnit.MILLIS))
            jedisPoolConfig.timeBetweenEvictionRuns = Duration.of(1000, ChronoUnit.MILLIS)
            jedisPoolConfig.softMinEvictableIdleDuration = Duration.of(1000, ChronoUnit.MILLIS)
            jedisPoolConfig.lifo = true
            jedisPoolConfig.numTestsPerEvictionRun = 50
            jedisPoolConfig.testOnBorrow = false
            jedisPoolConfig.testOnCreate = false
            jedisPoolConfig.minIdle = 10
            val serverConfig = config.serverConfig
            return JedisPool(jedisPoolConfig, serverConfig.host, serverConfig.port, 10000, config.password)
        }

        /**
         * Fetch a jedis instance using default config, which should be closed explicitly.
         *
         * @return jedis instance
         */
        @JvmStatic
        fun getJedis(): Jedis {
            return getJedis(defaultConfig)
        }

        @JvmStatic
        fun getJedis(config: RedisConfig): Jedis {
            if (!pools.containsKey(config)) {
                val pool0 = if (config.isSentinel) createSentinelPool(config) else createServerPool(config)
                val dbIndex = config.database
                pools[config] = Pair(pool0, dbIndex)
            }
            val pool = pools.getValue(config)
            val jedis = pool.first.resource
            jedis.select(pool.second)
            return jedis
        }

        @JvmStatic
        fun getJedis(configProvider: CoVaRedisConfigProvider): Jedis {
            return getJedis(configProvider.getConfig())
        }
    }
}
