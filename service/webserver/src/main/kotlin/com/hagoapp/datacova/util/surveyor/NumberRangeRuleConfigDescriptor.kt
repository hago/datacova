/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.surveyor

import com.hagoapp.datacova.lib.verification.VerifyConfiguration
import com.hagoapp.surveyor.RuleConfig
import com.hagoapp.surveyor.rule.NumberRangeRuleConfig
import java.util.Locale

class NumberRangeRuleConfigDescriptor internal constructor() : RuleConfigDescriptor() {

    companion object {
        private const val NUMBER_RANGE_RULE_TEMPLATE_DIR = "/validators/number_range"
    }

    override val templateDirName: String
        get() = NUMBER_RANGE_RULE_TEMPLATE_DIR

    override fun expectActualRuleConfigType(): Class<out RuleConfig> {
        return NumberRangeRuleConfig::class.java
    }

    override fun doDescribe(dt: DescriptionTemplate, config: VerifyConfiguration, locale: Locale): String {
        val st = dt.templateObject
        st.add("fields", config.fields)
        val nrConfig = config.ruleConfig as NumberRangeRuleConfig
        st.add("lowerBound", nrConfig.lowerBoundary)
        st.add("upperBound", nrConfig.upperBoundary)
        return st.render();
    }
}
