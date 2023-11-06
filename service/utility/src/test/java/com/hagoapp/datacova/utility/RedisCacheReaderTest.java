/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.utility.redis.GenericLoader;
import com.hagoapp.datacova.utility.redis.JedisManager;
import com.hagoapp.datacova.utility.redis.RedisCacheReader;
import com.hagoapp.datacova.utility.redis.RedisConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
class RedisCacheReaderTest {
    private static RedisConfig config;
    private static RedisCacheReader<String> reader;

    private static final String CACHE_NAME = "test_cache";
    private static final long CACHE_LIFE_SECOND = 5L;

    @BeforeAll
    static void init() {
        config = RedisTestConstants.createRedisConfig();
        reader = new RedisCacheReader.Builder<String>()
                .withJedisConfig(config)
                .shouldSkipCache(false)
                .withCacheName(CACHE_NAME)
                .withDataLifeTime(CACHE_LIFE_SECOND)
                .withType(String.class)
                .withLoadFunction(dataFunction)
                .create();
    }

    private static final GenericLoader<String> dataFunction = params -> Arrays.stream(params)
            .map(i -> i == null ? "null" : i.toString())
            .collect(Collectors.joining(", "));

    private static final GenericLoader<String> dataFunction2 = params -> Arrays.stream(params)
            .map(i -> i == null ? "null" : i.toString())
            .collect(Collectors.joining(";; "));
    private static final Gson gson = new GsonBuilder().create();

    private final Object[] params = {"ABC", 42, null, false};
    private final String expect = dataFunction.perform(params);
    private final String expect2 = dataFunction2.perform(params);

    @Test
    void testRedisCacheReader() throws InterruptedException {
        try (var jedis = JedisManager.getJedis(config)) {
            var v = reader.readData(params);
            var k = reader.getLastKey();
            var v1 = gson.fromJson(jedis.get(k), String.class);
            Assertions.assertEquals(expect, v);
            Assertions.assertEquals(v, v1);
            Thread.sleep((CACHE_LIFE_SECOND + 1) * 1000L);
            var v2 = jedis.get(k);
            Assertions.assertNull(v2);
        }
    }

    @Test
    void testRedisCacheReaderConvenientMethods() {
        try (var jedis = JedisManager.getJedis(config)) {
            RedisCacheReader.setRedisConfiguration(config);
            jedis.flushAll();
            var v = RedisCacheReader.readDataInCacheOnly(CACHE_NAME, String.class, params);
            Assertions.assertNull(v);
            log.debug("readDataInCacheOnly done");
            v = RedisCacheReader.readCachedData(CACHE_NAME, CACHE_LIFE_SECOND, dataFunction, String.class, params);
            Assertions.assertEquals(expect, v);
            var keys = jedis.keys("*");
            Assertions.assertFalse(keys.isEmpty());
            v = RedisCacheReader.readCachedData(CACHE_NAME, CACHE_LIFE_SECOND, dataFunction2, String.class, params);
            Assertions.assertEquals(expect, v);
            log.debug("readCachedData done");
            v = RedisCacheReader.readDataAndUpdateCache(CACHE_NAME, CACHE_LIFE_SECOND, dataFunction2, String.class, params);
            Assertions.assertEquals(expect2, v);
            log.debug("readDataAndUpdateCache done");
            v = RedisCacheReader.readDataAndClearCache(CACHE_NAME, dataFunction, String.class, params);
            Assertions.assertEquals(expect2, v);
            keys = jedis.keys("*");
            Assertions.assertTrue(keys.isEmpty());
            log.debug("readDataAndClearCache done");
        }
    }
}
