/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.surveyor

import com.hagoapp.datacova.verification.VerifyConfiguration
import com.hagoapp.surveyor.RuleConfig
import com.hagoapp.surveyor.rule.RelativeTimeBoundary
import com.hagoapp.surveyor.rule.RelativeTimeRangeConfig
import java.util.*

class RelativeTimeRangeRuleConfigDescriptor internal constructor() : RuleConfigDescriptor() {

    companion object {
        private const val RELATIVE_RANGE_RULE_TEMPLATE_DIR = "/validators/relative_time_range"
    }

    override val templateDirName: String
        get() = RELATIVE_RANGE_RULE_TEMPLATE_DIR

    override fun expectActualRuleConfigType(): Class<out RuleConfig> {
        return RelativeTimeRangeConfig::class.java
    }

    override fun doDescribe(dt: DescriptionTemplate, config: VerifyConfiguration, locale: Locale): String {
        val st = dt.templateObject
        st.add("fields", config.fields)
        val nrConfig = config.ruleConfig as RelativeTimeRangeConfig
        st.add("lowerBound", nrConfig.lowerBoundary)
        st.add("upperBound", nrConfig.upperBoundary)
        if (nrConfig.lowerBoundary != null) {
            st.add("isLower${nrConfig.lowerBoundary!!.anchor}", true)
            st.add("isLowerBefore", nrConfig.lowerBoundary!!.diff.isLaterThan)
        }
        if (nrConfig.upperBoundary != null) {
            st.add("isUpper${nrConfig.upperBoundary!!.anchor}", true)
            st.add("isUpperBefore", nrConfig.upperBoundary!!.diff.isLaterThan)
        }
        st.add("lowerTime", nrConfig.lowerBoundary?.calculateEpochMilli())
        st.add("upperTime", nrConfig.upperBoundary?.calculateEpochMilli())
        createDiffFlags("Lower", nrConfig.lowerBoundary).forEach { st.add(it.key, it.value) }
        createDiffFlags("Upper", nrConfig.upperBoundary).forEach { st.add(it.key, it.value) }
        return st.render()
    }

    private fun createDiffFlags(prefix: String, boundary: RelativeTimeBoundary?): Map<String, Boolean> {
        return if (boundary == null) java.util.Map.of()
        else mapOf(
            "is${prefix}Year" to (boundary.diff.year > 0),
            "is${prefix}YearPlural" to (boundary.diff.year > 1),
            "is${prefix}Month" to (boundary.diff.month > 0),
            "is${prefix}MonthPlural" to (boundary.diff.month > 1),
            "is${prefix}Day" to (boundary.diff.day > 0),
            "is${prefix}DayPlural" to (boundary.diff.day > 1),
            "is${prefix}Hour" to (boundary.diff.hour > 0),
            "is${prefix}HourPlural" to (boundary.diff.hour > 1)
        )
    }
}
