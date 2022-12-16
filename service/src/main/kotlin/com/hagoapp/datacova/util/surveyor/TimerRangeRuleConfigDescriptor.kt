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
import com.hagoapp.surveyor.rule.TimeRangeRuleConfig
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class TimerRangeRuleConfigDescriptor internal constructor() : RuleConfigDescriptor() {

    companion object {
        private const val TIME_RANGE_RULE_TEMPLATE_DIR = "/validators/time_range"
    }

    override val templateDirName: String
        get() = TIME_RANGE_RULE_TEMPLATE_DIR

    override fun expectActualRuleConfigType(): Class<out RuleConfig> {
        return TimeRangeRuleConfig::class.java
    }

    override fun doDescribe(dt: DescriptionTemplate, config: VerifyConfiguration, locale: Locale): String {
        val st = dt.templateObject
        st.add("fields", config.fields);
        val trConfig = config.ruleConfig as TimeRangeRuleConfig
        st.add("lowerBound", trConfig.lowerBoundary)
        st.add("upperBound", trConfig.upperBoundary)
        st.add("lowerTime", getEpochMilliString(trConfig.lowerBoundary?.timeStamp))
        st.add("upperTime", getEpochMilliString(trConfig.upperBoundary?.timeStamp))
        return st.render()
    }

    private fun getEpochMilliString(epochMilli: Long?): String? {
        epochMilli ?: return null
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault()).toString()
    }
}
