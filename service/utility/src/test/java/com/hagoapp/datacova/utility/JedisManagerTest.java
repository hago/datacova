/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility;

import com.hagoapp.datacova.utility.redis.JedisManager;
import com.hagoapp.datacova.utility.redis.RedisConfig;
import com.hagoapp.datacova.utility.redis.RedisSentinelConfig;
import com.hagoapp.datacova.utility.redis.RedisServerConfig;
import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
class JedisManagerTest {

    private static RedisConfig config;

    @BeforeAll
    static void init() {
        var props = System.getProperties();
        var host = System.getProperty(RedisTestConstants.REDIS_HOST, RedisTestConstants.DEFAULT_HOST);
        var port = !props.contains(RedisTestConstants.REDIS_PORT) ? RedisTestConstants.DEFAULT_PORT :
                Integer.getInteger(System.getProperty(RedisTestConstants.REDIS_PORT));
        var db = !props.contains(RedisTestConstants.REDIS_DATABASE) ? RedisTestConstants.DEFAULT_DATABASE :
                Integer.getInteger(System.getProperty(RedisTestConstants.REDIS_DATABASE));
        var password = System.getProperty(RedisTestConstants.REDIS_PASSWORD, RedisTestConstants.DEFAULT_PASSWORD);
        var srvConfig = new RedisServerConfig(host, port);
        var sentinelNodes = props.entrySet().stream()
                .filter(e -> e.getKey().toString().startsWith(RedisTestConstants.SENTINEL_HOST_PORT))
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        e -> Integer.getInteger(e.getValue().toString()))
                );
        var sentinelMaster = System.getProperty(RedisTestConstants.SENTINEL_MASTER, "");
        var sentinelConfig = new RedisSentinelConfig(sentinelNodes, sentinelMaster);
        var isSentinel = !sentinelConfig.getNodes().isEmpty();
        config = new RedisConfig(isSentinel, srvConfig, sentinelConfig, password, db);
        log.debug("Test with redis config: {}", config);
    }

    @Test
    void testKeyValue() {
        try (var jedis = JedisManager.getJedis(config)) {
            jedis.flushAll();
            var values = IntStream.range(0, 10000)
                    .mapToObj(i -> new Pair<>(String.format("key %d", i), String.format("value %d", i)))
                    .collect(Collectors.toList());
            values.forEach(i -> jedis.set(i.getFirst(), i.getSecond()));
            for (var i: values) {
                var v = jedis.get(i.getFirst());
                Assertions.assertEquals(i.getSecond(), v);
            }
            values.forEach(i -> jedis.del(i.getFirst()));
            var keys = jedis.keys("*");
            Assertions.assertTrue(keys.isEmpty());
            jedis.flushAll();
        }
    }
}
