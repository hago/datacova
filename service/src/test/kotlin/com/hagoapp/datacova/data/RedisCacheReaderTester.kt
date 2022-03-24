/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.RedisCacheReader.GenericLoader
import com.hagoapp.datacova.data.redis.RedisConfig
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class RedisCacheReaderTester {

    companion object {
        private val configFile: String = System.getProperty("cfg") ?: "./config.sample.json"
        private val logger = CoVaLogger.getLogger()
        private var dataLoadCount: Int = 0
        private lateinit var redisConfig: RedisConfig

        @BeforeAll
        @JvmStatic
        fun init() {
            CoVaConfig.loadConfig(configFile)
            redisConfig = CoVaConfig.getConfig().redis
        }
    }

    @Test
    fun regularTest() {
        dataLoadCount = 0
        val builder = RedisCacheReader.Builder<String>()
            .shouldSkipCache(false)
            .withLoadFunction(regularLoader)
            .withDataLifeTime(5)
            .withCacheName("RegularTest")
            .withJedisConfig(redisConfig)
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
        CoVaConfig.loadConfig(configFile)
        val builder = RedisCacheReader.Builder<TestClass>()
            .shouldSkipCache(false)
            .withLoadFunction(regularLoaderObject)
            .withDataLifeTime(5)
            .withType(TestClass::class.java)
            .withCacheName("RegularTestObject")
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

    private val regularLoaderObject: GenericLoader<TestClass> = object : GenericLoader<TestClass> {
        override fun perform(vararg params: Any?): TestClass {
            dataLoadCount++
            return TestClass(
                a = params[0] as Int,
                b = params[1] as String,
                c = params[2] as Double,
                d = params[3] as Boolean,
                e = params[4] as TestClass?
            )
        }
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
            "CompanionObject", 10, regularLoaderObject, TestClass::class.java, 1, "b", 2.01, false, null
        )
        Assertions.assertNotNull(s)
        Assertions.assertTrue(s is TestClass)
        Assertions.assertEquals(1, dataLoadCount)
        s = RedisCacheReader.readDataAndUpdateCache(
            "CompanionObject", 10, regularLoaderObject, TestClass::class.java, 1, "b", 2.01, false, null
        )
        Assertions.assertNotNull(s)
        Assertions.assertTrue(s is TestClass)
        Assertions.assertEquals(2, dataLoadCount)
        s = RedisCacheReader.readDataAndClearCache(
            "CompanionObject", regularLoaderObject, TestClass::class.java, 1, "b", 2.01, false, null
        )
        Assertions.assertNotNull(s)
        Assertions.assertTrue(s is TestClass)
        Assertions.assertEquals(3, dataLoadCount)
        s = RedisCacheReader.readCachedData(
            "CompanionObject", 10, regularLoaderObject, TestClass::class.java, 1, "b", 2.01, false, null
        )
        Assertions.assertNotNull(s)
        Assertions.assertTrue(s is TestClass)
        Assertions.assertEquals(4, dataLoadCount)
    }

}
