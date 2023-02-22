/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.surveyor

import com.hagoapp.surveyor.RuleConfig
import com.hagoapp.surveyor.rule.EmbedJythonRuleConfig
import com.hagoapp.surveyor.utils.EmbedPythonHelper
import org.slf4j.LoggerFactory

class EmbedJythonSurveyor : Surveyor {

    companion object {
        private const val RETURN_VARIABLE = "ret"
    }

    private lateinit var config: EmbedJythonRuleConfig
    private lateinit var py: EmbedPythonHelper
    private val logger = LoggerFactory.getLogger(EmbedPythonHelper::class.java)

    override fun getSupportedConfigType(): String {
        return EmbedJythonRuleConfig.EMBED_PYTHON_RULE_CONFIG
    }

    override fun acceptConfiguration(ruleConfig: RuleConfig): Surveyor {
        if (ruleConfig !is EmbedJythonRuleConfig) {
            throw UnsupportedOperationException("Not an EmbedPythonRuleConfig but ${ruleConfig::class.java.canonicalName}")
        }
        config = ruleConfig
        py = EmbedPythonHelper()
        py.allowedImportModules = config.allowedImports.toSet()
        return this
    }

    override fun process(params: MutableList<Any>): Boolean {
        val map = params.filterIndexed { i, _ -> i % 2 == 0 }.map { it.toString() }
            .zip(params.filterIndexed { i, _ -> i % 2 == 1 }).toMap()
        val result = py.execCodeBlock(config.snippet, map, setOf(RETURN_VARIABLE))
        return result[RETURN_VARIABLE]?.toString()?.toBoolean() ?: false
    }

    override fun close() {
        try {
            py.close()
        } catch (e: Throwable) {
            logger.error("Free jython with error: ${e.message}")
        }
    }
}
