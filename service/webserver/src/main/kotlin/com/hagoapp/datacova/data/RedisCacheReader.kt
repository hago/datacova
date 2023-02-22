/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.hagoapp.datacova.Application
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.redis.JedisManager
import com.hagoapp.datacova.data.redis.RedisConfig
import redis.clients.jedis.Jedis
import java.lang.reflect.Type

class RedisCacheReader<T> private constructor() {
    companion object {
        const val DEFAULT_VALIDITY = 3600L
        private val skipCache = !Application.productionMode

        @JvmStatic
        fun <T> readCachedData(
            cacheName: String,
            dataLifetime: Long,
            loader: GenericLoader<T>,
            type: Type,
            vararg params: Any?
        ): T? {
            val builder = Builder<T>()
                .shouldSkipCache(skipCache)
                .withLoadFunction(loader)
                .withDataLifeTime(dataLifetime)
                .withCacheName(cacheName)
                .withType(type)
            val reader = builder.create()
            return reader.readData(*params)
        }

        @JvmStatic
        fun <T> readDataInCacheOnly(
            cacheName: String,
            type: Type,
            vararg params: Any?
        ): T? {
            val builder = Builder<T>()
                .shouldSkipCache(skipCache)
                .withCacheName(cacheName)
                .withType(type)
            val reader = builder.create()
            return reader.readData(*params)
        }

        @JvmStatic
        fun <T> readDataAndUpdateCache(
            cacheName: String,
            dataLifetime: Long,
            loader: GenericLoader<T>,
            type: Type,
            vararg params: Any?
        ): T? {
            val builder = Builder<T>()
                .shouldSkipCache(skipCache)
                .withLoadFunction(loader)
                .withDataLifeTime(dataLifetime)
                .withCacheName(cacheName)
                .withType(type)
            val reader = builder.create()
            return reader.readData(*params)
        }

        @JvmStatic
        fun <T> readDataAndClearCache(
            cacheName: String,
            loader: GenericLoader<T>,
            type: Type,
            vararg params: Any?
        ): T? {
            val builder = Builder<T>()
                .shouldSkipCache(skipCache)
                .withLoadFunction(loader)
                .withDataLifeTime(-1)
                .withCacheName(cacheName)
                .withType(type)
            val reader = builder.create()
            return reader.readData(*params)
        }

        @JvmStatic
        fun clearData(cacheName: String, vararg params: Any?) {
            val builder = Builder<Any>()
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
        get() = field ?: CoVaConfig.getConfig().redis

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

        fun create(): RedisCacheReader<T> {
            return reader
        }
    }

    interface GenericLoader<T> {
        fun perform(vararg params: Any?): T?
        fun performBatch(paramsBatch: List<List<Any?>>): List<T?> {
            return paramsBatch.map { params -> perform(*params.toTypedArray()) }
        }
    }

    private fun createJedis(): Jedis {
        return JedisManager.getJedis(redisConfig!!)
    }

    fun readData(vararg params: Any?): T? {
        actualKey = key ?: createKey(*params)
        createJedis().use { jedis ->
            return when {
                loadFunction == null -> {
                    val jsonStr = jedis.get(actualKey)
                    if (jsonStr == null) null else Gson().fromJson<T>(jsonStr, type)
                }
                skipCache -> doDataLoadAndUpdateRedis(jedis, actualKey, *params)
                else -> {
                    val jsonStr = jedis.get(actualKey)
                    if (jsonStr == null) {
                        doDataLoadAndUpdateRedis(jedis, actualKey, *params)
                    } else {
                        Gson().fromJson<T>(jsonStr, type)
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
            val jsonStr = createGson().toJson(data)
            when {
                dataLifeTime == 0L -> jedis.set(key, jsonStr)
                dataLifeTime > 0L -> jedis.setex(key, dataLifeTime, jsonStr)
                else -> jedis.del(key)
            }
            data
        } catch (e: JsonSyntaxException) {
            null
        }
    }

    private fun createGson(): Gson {
        return GsonBuilder().serializeNulls().create()
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
        //println("clear key $actualKey")
        createJedis().use { it.del(actualKey) }
    }
}
