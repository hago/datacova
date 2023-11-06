package com.hagoapp.datacova.surveyor.rule

import com.hagoapp.datacova.surveyor.RuleConfig

class TimeRangeRuleConfig(
    var lowerBoundary: TimeBoundary? = null,
    var upperBoundary: TimeBoundary? = null
) : RuleConfig() {
    companion object {
        const val TIME_RANGE_CONFIG = "com.hagoapp.time.range"
    }

    override fun getConfigType(): String {
        return TIME_RANGE_CONFIG
    }
}
