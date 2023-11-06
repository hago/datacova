/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.surveyor.surveyor

import com.hagoapp.datacova.surveyor.RuleConfig
import com.hagoapp.datacova.surveyor.rule.EmbedJsRuleConfig
import com.hagoapp.datacova.surveyor.EmbedJsFunctionHelper
import org.slf4j.LoggerFactory

class EmbedJsSurveyor : Surveyor {

    private lateinit var conf: EmbedJsRuleConfig
    private lateinit var function: EmbedJsFunctionHelper
    private val logger = LoggerFactory.getLogger(EmbedJsSurveyor::class.java)

    override fun getSupportedConfigType(): String {
        return EmbedJsRuleConfig.EMBED_JS_RULE_CONFIG
    }

    override fun acceptConfiguration(ruleConfig: RuleConfig): Surveyor {
        if (ruleConfig !is EmbedJsRuleConfig) {
            throw UnsupportedOperationException("Not a EmbedJsRuleConfig")
        }
        conf = ruleConfig
        function = EmbedJsFunctionHelper(ruleConfig.snippet)
        return this
    }

    override fun process(params: MutableList<Any>): Boolean {
        return try {
            if ((conf.paramCount != null) && (conf.paramCount != params.size)) {
                throw IllegalArgumentException("expect ${conf.paramCount} parameters, ${params.size} given")
            }
            val result = function.execute(*params.toTypedArray())
            logger.debug("result: {}", result)
            if (result !is Boolean) {
                throw UnsupportedOperationException("Expect a boolean return value, however '$result' was returned")
            }
            return result
        } catch (e: Throwable) {
            logger.error("Execution of js snippet error: {}, source: {}", e.message, conf.snippet)
            false
        }
    }

    override fun close() {
        try {
            super.close()
            function.close()
        } catch (e: Throwable) {
            logger.error("Releasing Graal js error {}", e.message)
        }
    }
}
