/*
 *  Copyright (c) 2021.
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.rule

import com.hagoapp.surveyor.RuleConfig

class EmbedJythonRuleConfig : RuleConfig() {
    companion object {
        const val EMBED_PYTHON_RULE_CONFIG = "com.hagoapp.embed.jython"
    }

    override fun getConfigType(): String {
        return EMBED_PYTHON_RULE_CONFIG
    }

    var snippet: String = ""
    val allowedImports = mutableListOf<String>()
}
