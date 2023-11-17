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
import com.hagoapp.datacova.surveyor.rule.EmbedJythonRuleConfig
import com.hagoapp.datacova.worker.executor.surveyordescriptor.RuleConfigDescriptor
import java.util.*

class EmbedJythonRuleConfigDescriptor internal constructor() : RuleConfigDescriptor() {

    companion object {
        private const val EMBED_PYTHON_TEMPLATE_DIR = "/validators/pythonscript"
    }

    override val templateDirName: String
        get() = EMBED_PYTHON_TEMPLATE_DIR

    override fun expectActualRuleConfigType(): Class<out RuleConfig> {
        return EmbedJythonRuleConfig::class.java
    }

    override fun doDescribe(dt: DescriptionTemplate, config: VerifyConfiguration, locale: Locale): String {
        val st = dt.templateObject
        st.add("fields", config.fields);
        val jythonConfig = config.ruleConfig as EmbedJythonRuleConfig
        st.add("snippet", jythonConfig.snippet);
        return st.render();
    }
}
