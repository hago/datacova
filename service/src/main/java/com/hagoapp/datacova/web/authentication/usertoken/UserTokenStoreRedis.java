/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.authentication.usertoken;

import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.data.redis.JedisManager;
import com.hagoapp.datacova.data.redis.RedisConfig;
import com.hagoapp.datacova.user.UserInfo;
import redis.clients.jedis.Jedis;

import java.util.Set;

public class UserTokenStoreRedis implements UserTokenStore {

    private final RedisConfig config;
    private final long TOKEN_LIFE = 3600L;

    public UserTokenStoreRedis(RedisConfig config) {
        this.config = config;
    }

    @Override
    public void storeUserToken(String token, UserInfo userInfo) {
        try (var redis = JedisManager.getJedis(config)) {
            redis.setex(token, TOKEN_LIFE, userInfo.toJson());
            redis.hset(userInfo.toString(), token, "1");
        }
    }

    @Override
    public void removeUserToken(String token) {
        try (var redis = JedisManager.getJedis(config)) {
            String json = redis.get(token);
            if (json != null) {
                redis.del(token);
                UserInfo userInfo = string2UserInfo(json);
                redis.hdel(userInfo.toString(), token);
            }
        }
    }

    @Override
    public UserInfo findUserToken(String token) {
        try (var redis = JedisManager.getJedis(config)) {
            String json = redis.get(token);
            if (json == null) {
                return null;
            } else {
                return string2UserInfo(json);
            }
        }
    }

    private UserInfo string2UserInfo(String s) {
        return new GsonBuilder().create().fromJson(s, UserInfo.class);
    }

    @Override
    public Set<String> findTokensOfUser(UserInfo userInfo) {
        try (var redis = JedisManager.getJedis(config)) {
            var l = redis.hgetAll(userInfo.toString()).keySet();
            return l;
        }
    }
}
