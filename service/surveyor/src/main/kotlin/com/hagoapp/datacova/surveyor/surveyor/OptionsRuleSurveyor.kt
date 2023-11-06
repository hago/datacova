/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.surveyor.surveyor

import com.hagoapp.datacova.surveyor.RuleConfig
import com.hagoapp.datacova.surveyor.rule.OptionsRuleConfig

/**
 * The class implements a surveyor that check whether input values exist in predefined list.
 *
 * @author Chaojun Sun
 * @since 0.1
 */
class OptionsRuleSurveyor : Surveyor {

    private lateinit var config: OptionsRuleConfig
    private lateinit var surveyFunc: (input: String?) -> Boolean

    override fun getSupportedConfigType(): String {
        return OptionsRuleConfig.OPTIONS_RULE_CONFIG
    }

    override fun acceptConfiguration(ruleConfig: RuleConfig): Surveyor {
        if (ruleConfig !is OptionsRuleConfig) {
            throw UnsupportedOperationException("Not an OptionsRuleConfig, but ${ruleConfig::class.java.canonicalName}")
        }
        config = ruleConfig
        surveyFunc = if (ruleConfig.caseSensitive) config.options::contains
        else { input ->
            config.options.any { ((input == null) && (it == null)) || input.equals(it, true) }
        }
        return this
    }

    override fun process(params: MutableList<Any?>): Boolean {
        val p = params.map { it?.toString() }
        for (item in p) {
            if ((item == null) && (!config.isNullable)) {
                return false
            }
            if (!surveyFunc(item)) {
                return false
            }
        }
        return true
    }

}
