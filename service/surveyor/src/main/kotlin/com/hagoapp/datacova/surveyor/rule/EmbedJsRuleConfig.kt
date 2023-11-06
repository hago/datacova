/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.surveyor.rule

import com.hagoapp.datacova.surveyor.RuleConfig

class EmbedJsRuleConfig : RuleConfig() {
    companion object {
        const val EMBED_JS_RULE_CONFIG = "com.hagoapp.embed.js"
    }

    override fun getConfigType(): String {
        return EMBED_JS_RULE_CONFIG
    }

    var snippet: String = ""
    var description: String = ""
    var paramCount: Int? = null
}
