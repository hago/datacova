/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility;

import com.hagoapp.datacova.utility.redis.JedisManager;
import com.hagoapp.datacova.utility.redis.RedisConfig;
import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
class JedisManagerTest {

    private static RedisConfig config;

    @BeforeAll
    static void init() {
        config = RedisTestConstants.createRedisConfig();
    }

    @Test
    void testKeyValue() {
        try (var jedis = JedisManager.getJedis(config)) {
            jedis.flushAll();
            var values = IntStream.range(0, 10000)
                    .mapToObj(i -> new Pair<>(String.format("key %d", i), String.format("value %d", i)))
                    .collect(Collectors.toList());
            values.forEach(i -> jedis.set(i.getFirst(), i.getSecond()));
            for (var i : values) {
                var v = jedis.get(i.getFirst());
                Assertions.assertEquals(i.getSecond(), v);
            }
            values.forEach(i -> jedis.del(i.getFirst()));
            var keys = jedis.keys("*");
            Assertions.assertTrue(keys.isEmpty());
            jedis.flushAll();
        }
    }

    @Test
    void testMassiveInvoking() throws InterruptedException {
        var count = 10000;
        AtomicInteger success = new AtomicInteger();
        int concurrency = 10;
        var pool = Executors.newFixedThreadPool(concurrency);
        for (var i = 0; i < count; i++) {
            final var j = i;
            pool.execute(() -> {
                try (var jedis = JedisManager.getJedis(config)) {
                    var pong = jedis.ping();
                    log.debug("No. {} ping: {}", j, pong);
                    success.getAndIncrement();
                } catch (Exception e) {
                    log.error("testMassiveInvoking {}", e.getMessage());
                    StackTraceWriter.write(e, log);
                }
            });
        }
        pool.shutdown();
        while (!pool.isTerminated()) {
            Thread.sleep(200);
        }
        log.info("success cont: {}", success);
        Assertions.assertEquals(success.get(), count);
    }
}
