/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.redis

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.hagoapp.datacova.utility.CoVaException
import org.slf4j.LoggerFactory
import redis.clients.jedis.Jedis
import java.lang.reflect.Type

class RedisCacheReader<T> private constructor() {
    companion object {
        const val DEFAULT_VALIDITY = 3600L
        var skipCache = false
        private val logger = LoggerFactory.getLogger(RedisCacheReader::class.java)
        private val gson = GsonBuilder().serializeNulls().create()

        @JvmStatic
        fun <T> readCachedData(
            redisConfig: RedisConfig,
            cacheName: String,
            dataLifetime: Long,
            loader: GenericLoader<T>,
            deserializer: Deserializer<T>?,
            type: Type,
            vararg params: Any?
        ): T? {
            val builder = Builder<T>()
                .withJedisConfig(redisConfig)
                .shouldSkipCache(skipCache)
                .withLoadFunction(loader)
                .withDataLifeTime(dataLifetime)
                .withCacheName(cacheName)
                .withDeserializer(deserializer ?: Deserializer { json -> Gson().fromJson(json, type) })
                .withType(type)
            val reader = builder.create()
            return reader.readData(*params)
        }

        @JvmStatic
        fun <T> readDataInCacheOnly(
            redisConfig: RedisConfig,
            cacheName: String,
            type: Type,
            deserializer: Deserializer<T>?,
            vararg params: Any?
        ): T? {
            val builder = Builder<T>()
                .withJedisConfig(redisConfig)
                .shouldSkipCache(skipCache)
                .withCacheName(cacheName)
                .withDeserializer(deserializer ?: Deserializer { json -> Gson().fromJson(json, type) })
                .withType(type)
            val reader = builder.create()
            return reader.readData(*params)
        }

        @JvmStatic
        fun <T> readDataAndUpdateCache(
            redisConfig: RedisConfig,
            cacheName: String,
            dataLifetime: Long,
            loader: GenericLoader<T>,
            deserializer: Deserializer<T>?,
            type: Type,
            vararg params: Any?
        ): T? {
            val builder = Builder<T>()
                .withJedisConfig(redisConfig)
                .shouldSkipCache(true)
                .withLoadFunction(loader)
                .withDataLifeTime(dataLifetime)
                .withCacheName(cacheName)
                .withDeserializer(deserializer ?: Deserializer { json -> Gson().fromJson(json, type) })
                .withType(type)
            val reader = builder.create()
            return reader.readData(*params)
        }

        @JvmStatic
        fun <T> readDataAndClearCache(
            redisConfig: RedisConfig,
            cacheName: String,
            loader: GenericLoader<T>,
            type: Type,
            vararg params: Any?
        ): T? {
            val builder = Builder<T>()
                .withJedisConfig(redisConfig)
                .shouldSkipCache(skipCache)
                .withLoadFunction(loader)
                .withDataLifeTime(-1)
                .withCacheName(cacheName)
                .withType(type)
            val reader = builder.create()
            val value = reader.readData(*params)
            reader.clearData(*params)
            return value
        }

        @JvmStatic
        fun clearData(redisConfig: RedisConfig, cacheName: String, vararg params: Any?) {
            val builder = Builder<String>()
                .withJedisConfig(redisConfig)
                .shouldSkipCache(skipCache)
                .withDataLifeTime(-1)
                .withCacheName(cacheName)
            val reader = builder.create()
            return reader.clearData(*params)
        }

        @JvmStatic
        fun createCacheKey(identity: String, vararg params: Any?): String {
            if (params.isEmpty()) {
                return identity
            }
            val paramIdentity = params.filterNotNull().joinToString("|") { it.toString() }
            return "$identity||$paramIdentity"
        }

    }

    private var dataLifeTime: Long = 0L
    private var key: String? = null
    private var type: Type = String::class.java
    private var loadFunction: GenericLoader<T>? = null
    private var skipCache: Boolean = false
    private var cacheName: String? = null
    private lateinit var actualKey: String
    private var redisConfig: RedisConfig? = null
    private var deserializer: Deserializer<T> = Deserializer<T> { json -> gson.fromJson<T>(json, type) }

    val lastKey: String
        get() = actualKey

    class Builder<T> {
        private val reader = RedisCacheReader<T>()

        fun withDataLifeTime(lifeTime: Long): Builder<T> {
            reader.dataLifeTime = when {
                lifeTime < 0 -> DEFAULT_VALIDITY
                else -> lifeTime
            }
            return this
        }

        fun withKey(key: String): Builder<T> {
            reader.key = key
            return this
        }

        fun withType(type: Type): Builder<T> {
            reader.type = type
            return this
        }

        fun withLoadFunction(function: GenericLoader<T>?): Builder<T> {
            reader.loadFunction = function
            return this
        }

        fun shouldSkipCache(skip: Boolean): Builder<T> {
            reader.skipCache = skip
            return this
        }

        fun withCacheName(name: String): Builder<T> {
            reader.cacheName = name
            return this
        }

        fun withJedisConfig(redisConfig: RedisConfig): Builder<T> {
            reader.redisConfig = redisConfig
            return this
        }

        fun withDeserializer(deserializer: Deserializer<T>): Builder<T> {
            reader.deserializer = deserializer
            return this
        }

        fun create(): RedisCacheReader<T> {
            return reader
        }
    }

    private fun createJedis(): Jedis {
        return when {
            redisConfig != null -> JedisManager.getJedis(redisConfig!!)
            else -> throw CoVaException("Redis Configuration is null!")
        }
    }

    fun readData(vararg params: Any?): T? {
        actualKey = key ?: createKey(*params)
        createJedis().use { jedis ->
            return when {
                loadFunction == null -> {
                    val jsonStr = jedis[actualKey]
                    if (jsonStr == null) null else Gson().fromJson<T>(jsonStr, type)
                }

                skipCache -> doDataLoadAndUpdateRedis(jedis, actualKey, *params)
                else -> {
                    val jsonStr = jedis[actualKey]
                    if (jsonStr == null) {
                        doDataLoadAndUpdateRedis(jedis, actualKey, *params)
                    } else {
                        deserializer.deserialize(jsonStr)
                    }
                }
            }
        }
    }

    fun readBatchData(paramsBatch: List<List<Any?>>): List<T?> {
        val keys = paramsBatch.map { createKey(it.toTypedArray()) }
        val gson = GsonBuilder().create()
        createJedis().use { jedis ->
            val current = if (skipCache) paramsBatch.map { null }
            else jedis.mget(*keys.toTypedArray()).map { if (it == "nil") null else it }
            val indexesOfNotFound = current.mapIndexed { i, s -> Pair(i, s) }
                .filter { it.second == null }.map { it.first }
            val missing = indexesOfNotFound.map { index -> Pair(index, paramsBatch[index]) }
            val supplements = if (missing.isNotEmpty()) {
                val paramsMissing = missing.map { it.second }
                val fetchData = loadFunction?.performBatch(paramsMissing) ?: paramsBatch.map { null }
                val batchSetSequence = fetchData.mapIndexedNotNull { i, item ->
                    val k = createKey(*paramsMissing[i].toTypedArray())
                    listOf(k, gson.toJson(item))
                }.flatten()
                jedis.mset(*batchSetSequence.toTypedArray())
                missing.associate { (i, _) -> Pair(i, fetchData[i]) }
            } else mapOf()
            return current.mapIndexed { i, s ->
                when {
                    s != null -> gson.fromJson<T>(s, type)
                    supplements.containsKey(i) -> supplements.getValue(i)
                    else -> null
                }
            }
        }
    }

    private fun doDataLoadAndUpdateRedis(jedis: Jedis, key: String, vararg params: Any?): T? {
        val data = loadFunction?.perform(*params) ?: return null
        return try {
            val jsonStr = gson.toJson(data)
            when {
                dataLifeTime == 0L -> jedis[key] = jsonStr
                dataLifeTime > 0L -> jedis.setex(key, dataLifeTime, jsonStr)
                else -> jedis.del(key)
            }
            data
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    private fun createKey(vararg params: Any?): String {
        return when {
            cacheName != null -> createCacheKey(cacheName!!, *params)
            (loadFunction != null) && (loadFunction!!::class.java.canonicalName != null) -> createCacheKey(
                loadFunction!!::class.java.canonicalName, *params
            )

            else -> createCacheKey("", *params)
        }
    }

    fun clearData(vararg params: Any?) {
        actualKey = key ?: createKey(*params)
        logger.debug("clear key {}", actualKey)
        createJedis().use { it.del(actualKey) }
    }
}
