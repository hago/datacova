package com.hagoapp.surveyor.rule

import com.hagoapp.surveyor.RuleConfig

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
