/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.rules

import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.lib.data.ValidationRuleData
import com.hagoapp.datacova.lib.verification.Rule
import com.hagoapp.datacova.utility.redis.JedisManager
import com.hagoapp.datacova.utility.redis.RedisCacheReader

class ValidationRuleCache {
    companion object {

        private const val VALIDATION_RULE_LIST = "VALIDATION_RULE_LIST"
        private const val VALIDATION_RULE = "VALIDATION_RULE"

        fun getRules(workspaceId: Int, start: Int, size: Int): List<Rule> {
            val token = object : TypeToken<List<Rule>>() {}
            val l = RedisCacheReader.readCachedData(
                VALIDATION_RULE_LIST,
                86400,
                { params -> ValidationRuleData(CoVaConfig.getConfig().database).getRules(params[0] as Int, params[1] as Int, params[2] as Int) },
                token.type,
                workspaceId,
                start,
                size
            )
            return l!!
        }

        fun clearRules() {
            JedisManager.getJedis(CoVaConfig.getConfig().redis).use { jedis ->
                val keys = jedis.keys("$VALIDATION_RULE_LIST*")
                if (keys.isNotEmpty()) {
                    jedis.del(*keys.toTypedArray())
                }
            }
        }

        fun getRule(id: Long): Rule? {
            return RedisCacheReader.readCachedData(
                VALIDATION_RULE,
                86400,
                { params -> ValidationRuleData(CoVaConfig.getConfig().database).getRule(params[0] as Long) },
                Rule::class.java, id
            )
        }

        fun clearRule(id: Long) {
            RedisCacheReader.clearData(VALIDATION_RULE, id)
        }
    }
}
