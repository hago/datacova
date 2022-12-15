/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.surveyor

import com.hagoapp.surveyor.RuleConfig
import com.hagoapp.surveyor.rule.EmbedJythonRuleConfig
import org.stringtemplate.v4.ST

class EmbedJythonRuleConfigDescriptor internal constructor() : RuleConfigDescriptor() {

    companion object {
        private const val EMBED_PYTHON_TEMPLATE_DIR = "/validators/pythonscript"
    }

    override val templateDirName: String
        get() = EMBED_PYTHON_TEMPLATE_DIR

    override fun getExpectActualRuleConfigType(): Class<out RuleConfig> {
        return EmbedJythonRuleConfig::class.java
    }

    override fun doDescribe(st: ST, config: RuleConfig): String {
        TODO("Not yet implemented")
    }
}
