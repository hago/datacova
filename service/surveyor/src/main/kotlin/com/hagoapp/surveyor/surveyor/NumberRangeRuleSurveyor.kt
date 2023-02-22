/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.surveyor

import com.hagoapp.surveyor.RuleConfig
import com.hagoapp.surveyor.rule.NumberRangeRuleConfig

/**
 * The implementation of number range surveyor. This surveyor assumes input sequences are all numbers and check each
 * of it against the number range configuration, and report true if all input values are in range, otherwise false.
 *
 * @author Chaojun Sun
 */
class NumberRangeRuleSurveyor : Surveyor {

    private lateinit var config: NumberRangeRuleConfig

    override fun getSupportedConfigType(): String {
        return NumberRangeRuleConfig.NUMBER_RANGE_CONFIG
    }

    override fun acceptConfiguration(ruleConfig: RuleConfig): Surveyor {
        if (ruleConfig !is NumberRangeRuleConfig) {
            throw UnsupportedOperationException("Not a number range config")
        }
        config = ruleConfig
        return this
    }

    override fun process(params: MutableList<Any>): Boolean {
        val p = params.toList()
        return when {
            p.isEmpty() -> true
            config.lowerBoundary == null && config.upperBoundary == null -> true
            config.lowerBoundary == null -> p.map { toNum(it) }.all { config.upperBoundary!!.gt(it) }
            config.upperBoundary == null -> p.map { toNum(it) }.all { config.lowerBoundary!!.lt(it) }
            else -> p.map { toNum(it) }.all { config.lowerBoundary!!.lt(it) && config.upperBoundary!!.gt(it) }
        }
    }

    private fun toNum(input: Any): Double {
        if (input !is Number) {
            throw UnsupportedOperationException("$input should be a number")
        } else {
            return input.toDouble()
        }
    }
}
