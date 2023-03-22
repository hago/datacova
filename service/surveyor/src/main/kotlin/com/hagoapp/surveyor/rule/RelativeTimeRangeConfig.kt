package com.hagoapp.surveyor.rule

import com.hagoapp.surveyor.RuleConfig

class RelativeTimeRangeConfig(
    var lowerBoundary: RelativeTimeBoundary? = null,
    var upperBoundary: RelativeTimeBoundary? = null
) : RuleConfig() {
    companion object {
        const val RELATIVE_TIME_RANGE_CONFIG = "com.hagoapp.relative.time.range"
    }

    override fun getConfigType(): String {
        return RELATIVE_TIME_RANGE_CONFIG
    }
}
