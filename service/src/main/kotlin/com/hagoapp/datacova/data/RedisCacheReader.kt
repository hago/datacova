package com.hagoapp.datacova.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.redis.RedisPool
import redis.clients.jedis.Jedis
import java.lang.reflect.Type

class RedisCacheReader<T> private constructor() {
    companion object {
        const val DEFAULT_VALIDITY = 60 * 5;
    }

    private var redis: RedisPool? = null
    private var dataLifeTime: Int = 0
    private var key: String? = null
    private var type: Type = String::class.java
    private lateinit var loadFunction: GenericLoader<T>
    private var skipCache: Boolean = false
    private var cacheName: String? = null

    class Builder<T> {
        private val reader = RedisCacheReader<T>()

        fun withRedis(redis: RedisPool): Builder<T> {
            reader.redis = redis
            return this
        }

        fun withDataLifeTime(lifeTime: Int): Builder<T> {
            reader.dataLifeTime = lifeTime
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

        fun withLoadFunction(function: GenericLoader<T>): Builder<T> {
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
        if (!this::loadFunction.isInitialized) {
            throw Exception("Data loading function is not defined")
        }
        if (redis == null) {
            redis = RedisPool(CoVaConfig.getConfig().redis)
        }
        redis!!.jedis.use { jedis ->
            val k = key ?: createKey(*params)
            return if (skipCache) {
                doDataLoadAndUpdateRedis(jedis, k, *params)
            } else {
                val jsonStr = jedis.get(k)
                if (jsonStr == null) {
                    doDataLoadAndUpdateRedis(jedis, k, *params)
                } else {
                    Gson().fromJson(jsonStr, type)
                }
            }
        }
    }

    private fun doDataLoadAndUpdateRedis(jedis: Jedis, key: String, vararg params: Any?): T? {
        val data = loadFunction.perform(*params) ?: return null
        return try {
            val jsonStr = createGson().toJson(data)
            when {
                dataLifeTime == 0 -> jedis.set(key, jsonStr)
                dataLifeTime > 0 -> jedis.setex(key, dataLifeTime, jsonStr)
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
        val paramIdentity = params.filterNotNull().joinToString("|") { it.toString() }
        return when {
            cacheName != null -> "$cacheName||$paramIdentity"
            loadFunction::class.java.canonicalName != null -> "${loadFunction::class.java.canonicalName}||$paramIdentity"
            else -> "||$paramIdentity"
        }
    }
}
