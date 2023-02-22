/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.JedisManagerTester.Companion.ENABLE_REDIS_TEST_FLAG
import com.hagoapp.datacova.data.redis.JedisManager
import com.hagoapp.datacova.data.redis.RedisConfig
import com.hagoapp.util.StackTraceWriter
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.slf4j.LoggerFactory

@EnabledIfSystemProperty(named = ENABLE_REDIS_TEST_FLAG, matches = ".*")
class JedisManagerTester {

    companion object {
        const val ENABLE_REDIS_TEST_FLAG = "cova.redis.test"
        private val configFile: String = System.getProperty("cfg") ?: "./config.sample.json"
        private val logger = LoggerFactory.getLogger(JedisManagerTester::class.java)
        private lateinit var redisConfig: RedisConfig

        @BeforeAll
        @JvmStatic
        fun init() {
            CoVaConfig.loadConfig(configFile)
            redisConfig = CoVaConfig.getConfig().redis
        }
    }

    @Test
    fun testMassiveInvoking() {
        //println(redisConfig)
        val count = 1000
        var success = 0
        for (i in 0 until count) {
            try {
                JedisManager.getJedis(redisConfig).use { redis ->
                    val pong = redis.ping()
                    println("No. $i ping: $pong")
                }
                success++
            } catch (e: Throwable) {
                logger.error("{}", e.message)
                StackTraceWriter.writeToLogger(e, logger)
            }
        }
        println(success)
        Assertions.assertEquals(success, count)
    }
}
