/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.data

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.redis.JedisManager
import com.hagoapp.datacova.data.redis.RedisConfig

class CoVaRedisConfigProvider: JedisManager.Companion.ConfigProvider {
    override fun getConfig(): RedisConfig {
        return CoVaConfig.getConfig().redis
    }
}
