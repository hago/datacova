/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility;

import com.hagoapp.datacova.utility.redis.RedisConfig;
import com.hagoapp.datacova.utility.redis.RedisSentinelConfig;
import com.hagoapp.datacova.utility.redis.RedisServerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
public class RedisTestConstants {
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 6379;
    public static final int DEFAULT_DATABASE = 0;
    public static final String DEFAULT_PASSWORD = null;

    public static final String REDIS_HOST = "redis.host";
    public static final String REDIS_PORT = "redis.port";
    public static final String REDIS_DATABASE = "redis.database";
    public static final String REDIS_PASSWORD = "redis.password";
    public static final String SENTINEL_HOST_PORT = "sentinel.host";
    public static final String SENTINEL_MASTER = "sentinel.master";

    public static RedisConfig createRedisConfig() {
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
        var config = new RedisConfig(isSentinel, srvConfig, sentinelConfig, password, db);
        log.debug("Test with redis config: {}", config);
        return config;
    }
}
