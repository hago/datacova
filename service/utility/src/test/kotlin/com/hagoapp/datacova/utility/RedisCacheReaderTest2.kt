/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility

import com.google.gson.GsonBuilder
import com.hagoapp.datacova.utility.redis.GenericLoader
import com.hagoapp.datacova.utility.redis.RedisCacheReader
import com.hagoapp.datacova.utility.redis.RedisConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class RedisCacheReaderTest2 {

    companion object {

        private val logger = LoggerFactory.getLogger(RedisCacheReaderTest2::class.java)
        private var dataLoadCount: Int = 0
        private lateinit var config: RedisConfig
        private lateinit var reader: RedisCacheReader<String>
        private const val CACHE_NAME = "test_cache"
        private const val CACHE_LIFE_SECOND = 5L

        @BeforeAll
        @JvmStatic
        fun init() {
            config = RedisTestConstants.createRedisConfig()
            reader = RedisCacheReader.Builder<String>()
                .withJedisConfig(config)
                .shouldSkipCache(false)
                .withCacheName(CACHE_NAME)
                .withDataLifeTime(CACHE_LIFE_SECOND)
                .withType(String::class.java)
                .withLoadFunction(dataFunction)
                .create()
        }

        private val dataFunction = GenericLoader { params: Array<Any?> ->
            params.joinToString(", ") { it?.toString() ?: "null" }
        }

        private val dataFunction2 = GenericLoader { params: Array<Any?> ->
            params.joinToString(";; ") { it?.toString() ?: "null" }
        }

        private val gson = GsonBuilder().create()
    }

    @Test
    fun regularTest() {
        dataLoadCount = 0
        val builder = RedisCacheReader.Builder<String>()
            .shouldSkipCache(false)
            .withLoadFunction(regularLoader)
            .withDataLifeTime(5)
            .withCacheName("RegularTest")
            .withJedisConfig(config)
        val reader = builder.create()
        var x = reader.readData("a", "b", "c")
        Assertions.assertNotNull(x)
        Assertions.assertTrue(x is String)
        Assertions.assertEquals(1, dataLoadCount)
        x = reader.readData("a", "b", "c")
        Assertions.assertNotNull(x)
        Assertions.assertEquals(1, dataLoadCount)
        Thread.sleep(1000 * 6)
        x = reader.readData("a", "b", "c")
        Assertions.assertNotNull(x)
        Assertions.assertEquals(2, dataLoadCount)
    }

    private val regularLoader: GenericLoader<String> = object : GenericLoader<String> {
        override fun perform(vararg params: Any?): String {
            dataLoadCount++
            return params.filterNotNull().joinToString(" + ")
        }
    }

    @Test
    fun regularTestClass() {
        dataLoadCount = 0
        val builder = RedisCacheReader.Builder<TestClass>()
            .shouldSkipCache(false)
            .withLoadFunction(regularLoaderObject)
            .withDataLifeTime(5)
            .withType(TestClass::class.java)
            .withCacheName("RegularTestObject")
            .withJedisConfig(config)
        val reader = builder.create()
        var x = reader.readData(1, "b", 2.01, false, null)
        Assertions.assertNotNull(x)
        Assertions.assertTrue(x is TestClass)
        Assertions.assertEquals(1, dataLoadCount)
        x = reader.readData(1, "b", 2.01, false, null)
        Assertions.assertNotNull(x)
        Assertions.assertEquals(1, dataLoadCount)
        Thread.sleep(1000 * 6)
        x = reader.readData(1, "b", 2.01, false, null)
        Assertions.assertNotNull(x)
        Assertions.assertEquals(2, dataLoadCount)
    }

    private val regularLoaderObject: GenericLoader<TestClass> =
        GenericLoader<TestClass> { params ->
            dataLoadCount++
            TestClass(
                a = params[0] as Int,
                b = params[1] as String,
                c = params[2] as Double,
                d = params[3] as Boolean,
                e = params[4] as TestClass?
            )
        }

    data class TestClass(
        val a: Int,
        val b: String,
        val c: Double,
        val d: Boolean,
        val e: TestClass?
    )

    @Test
    fun testCompanion() {
        dataLoadCount = 0
        var s = RedisCacheReader.readCachedData(
            config, "CompanionObject", 10, regularLoaderObject, null, TestClass::class.java, 1, "b", 2.01, false, null
        )
        Assertions.assertNotNull(s)
        Assertions.assertTrue(s is TestClass)
        Assertions.assertEquals(1, dataLoadCount)
        s = RedisCacheReader.readDataAndUpdateCache(
            config, "CompanionObject", 10, regularLoaderObject, null, TestClass::class.java, 1, "b", 2.01, false, null
        )
        Assertions.assertNotNull(s)
        Assertions.assertTrue(s is TestClass)
        Assertions.assertEquals(2, dataLoadCount)
        s = RedisCacheReader.readDataAndClearCache(
            config, "CompanionObject", regularLoaderObject, TestClass::class.java, 1, "b", 2.01, false, null
        )
        Assertions.assertNotNull(s)
        Assertions.assertTrue(s is TestClass)
        Assertions.assertEquals(2, dataLoadCount)
        s = RedisCacheReader.readCachedData(
            config, "CompanionObject", 10, regularLoaderObject, null, TestClass::class.java, 1, "b", 2.01, false, null
        )
        Assertions.assertNotNull(s)
        Assertions.assertTrue(s is TestClass)
        Assertions.assertEquals(3, dataLoadCount)
    }

}
