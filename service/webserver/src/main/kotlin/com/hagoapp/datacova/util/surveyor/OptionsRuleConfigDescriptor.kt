/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.surveyor

import com.hagoapp.datacova.lib.verification.VerifyConfiguration
import com.hagoapp.datacova.surveyor.RuleConfig
import com.hagoapp.datacova.surveyor.rule.OptionsRuleConfig
import java.util.*

class OptionsRuleConfigDescriptor internal constructor() : RuleConfigDescriptor() {

    companion object {
        private const val OPTIONS_RULE_TEMPLATE_DIR = "/validators/options"
    }

    override val templateDirName: String
        get() = OPTIONS_RULE_TEMPLATE_DIR

    override fun expectActualRuleConfigType(): Class<out RuleConfig> {
        return OptionsRuleConfig::class.java
    }

    override fun doDescribe(dt: DescriptionTemplate, config: VerifyConfiguration, locale: Locale): String {
        if (config.fields.isEmpty()) {
            throw UnsupportedOperationException("No field defined for rule")
        }
        val fieldString = config.fields.joinToString(", ") { """'${it.replace("'", "''")}'""" }
        val optConfig = config.ruleConfig as OptionsRuleConfig
        val optionString = optConfig.options.joinToString(", ") {
            if (it == null) "null"
            else """"${it.replace("\"", "\"\"")}""""
        }
        return dt.rawTemplate.format(locale, fieldString, optionString)
    }
}
