/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.redis.JedisManager
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class JedisManagerTester {

    private val configFile: String = System.getProperty("cfg") ?: "./config.sample.json"

    @Test
    fun testMassiveInvoking() {
        CoVaConfig.loadConfig(configFile)
        val redisConfig = CoVaConfig.getConfig().redis
        //println(redisConfig)
        val count = 1000
        val successRate = 0.98f
        var success = 0
        for (i in 0 until count) {
            try {
                JedisManager(redisConfig).use { redis ->
                    val pong = redis.jedis.ping()
                    println("No. $i ping: $pong")
                }
                success++
            } catch (e: Throwable) {
                //
            }
        }
        println(success)
        Assertions.assertTrue(success.toFloat() / count.toFloat() >= successRate)
    }
}
