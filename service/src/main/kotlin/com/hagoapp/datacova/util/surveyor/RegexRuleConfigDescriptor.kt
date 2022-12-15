/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.surveyor

import com.hagoapp.surveyor.RuleConfig
import com.hagoapp.surveyor.rule.RegexRuleConfig
import org.stringtemplate.v4.ST

class RegexRuleConfigDescriptor internal constructor() : RuleConfigDescriptor() {

    companion object {
        private const val REGEX_RULE_TEMPLATE_DIR = "/validators/regex"
    }

    override val templateDirName: String
        get() = REGEX_RULE_TEMPLATE_DIR

    override fun getExpectActualRuleConfigType(): Class<out RuleConfig> {
        return RegexRuleConfig::class.java
    }

    override fun doDescribe(st: ST, config: RuleConfig): String {
        TODO("Not yet implemented")
    }
}
