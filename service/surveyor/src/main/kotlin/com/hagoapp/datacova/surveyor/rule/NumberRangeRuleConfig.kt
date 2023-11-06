package com.hagoapp.datacova.surveyor.rule

import com.hagoapp.datacova.surveyor.RuleConfig

/**
 * The configuration of a number range surveyor.
 *
 * @property lowerBoundary  the boundary to define the minimum value, null means no minimum check
 * @property upperBoundary  the boundary to define the maximum value, null means no maximum check
 *
 * @author Chaojun Sun
 */
class NumberRangeRuleConfig : RuleConfig() {

    companion object {
        const val NUMBER_RANGE_CONFIG = "com.hagoapp.number.range"
    }

    val lowerBoundary: NumberBoundary? = null
    val upperBoundary: NumberBoundary? = null

    override fun getConfigType(): String {
        return NUMBER_RANGE_CONFIG
    }
}
