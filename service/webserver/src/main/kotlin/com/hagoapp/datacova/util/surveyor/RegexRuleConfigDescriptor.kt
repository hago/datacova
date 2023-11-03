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
import com.hagoapp.surveyor.rule.RegexRuleConfig
import java.util.*

class RegexRuleConfigDescriptor internal constructor() : RuleConfigDescriptor() {

    companion object {
        private const val REGEX_RULE_TEMPLATE_DIR = "/validators/regex"
    }

    override val templateDirName: String
        get() = REGEX_RULE_TEMPLATE_DIR

    override fun expectActualRuleConfigType(): Class<out RuleConfig> {
        return RegexRuleConfig::class.java
    }

    override fun doDescribe(dt: DescriptionTemplate, config: VerifyConfiguration, locale: Locale): String {
        val regexConfig = config.ruleConfig as RegexRuleConfig
        val params = mutableListOf("")
            .plus(if (config.fields.isEmpty()) listOf("") else config.fields).plus(regexConfig.pattern)
            .toTypedArray()
        return dt.rawTemplate.format(locale, params)
        // return String.format(dt.rawTemplate, params)
    }
}
