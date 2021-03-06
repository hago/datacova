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
import com.hagoapp.datacova.config.init.CoVaConfig
import com.hagoapp.datacova.data.redis.JedisManager
import redis.clients.jedis.Jedis
import java.lang.reflect.Type

class RedisCacheReader<T> private constructor() {
    companion object {
        const val DEFAULT_VALIDITY = 3600;
        private val skipCache = !Application.productionMode

        @JvmStatic
        fun <T> readCachedData(
            cacheName: String,
            dataLifetime: Int,
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
            dataLifetime: Int,
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

    private var redis: JedisManager? = null
    private var dataLifeTime: Int = 0
    private var key: String? = null
    private var type: Type = String::class.java
    private var loadFunction: GenericLoader<T>? = null
    private var skipCache: Boolean = false
    private var cacheName: String? = null
    private lateinit var actualKey: String

    val lastKey: String
        get() = actualKey

    class Builder<T> {
        private val reader = RedisCacheReader<T>()

        fun withRedis(redis: JedisManager): Builder<T> {
            reader.redis = redis
            return this
        }

        fun withDataLifeTime(lifeTime: Int): Builder<T> {
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

        fun create(): RedisCacheReader<T> {
            return reader
        }
    }

    interface GenericLoader<T> {
        fun perform(vararg params: Any?): T?
    }

    fun readData(vararg params: Any?): T? {
        if (redis != null) {
            return doRedisOps(redis!!, *params)
        } else {
            JedisManager(CoVaConfig.getConfig().redis).use {
                return doRedisOps(it, *params)
            }
        }
    }

    private fun doRedisOps(jedisManager: JedisManager, vararg params: Any?): T? {
        jedisManager.jedis.use { jedis ->
            actualKey = key ?: createKey(*params)
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

    private fun doDataLoadAndUpdateRedis(jedis: Jedis, key: String, vararg params: Any?): T? {
        val data = loadFunction?.perform(*params) ?: return null
        return try {
            val jsonStr = createGson().toJson(data)
            when {
                dataLifeTime == 0 -> jedis.set(key, jsonStr)
                dataLifeTime > 0 -> jedis.setex(key, dataLifeTime, jsonStr)
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
        when (redis) {
            null -> JedisManager(CoVaConfig.getConfig().redis).use { manager ->
                manager.jedis.use { it.del(actualKey) }
            }
            else -> redis!!.jedis.use { it.del(actualKey) }
        }
    }
}
