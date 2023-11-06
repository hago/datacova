package com.hagoapp.datacova.surveyor.rule

import com.hagoapp.datacova.surveyor.RuleConfig

/**
 * This class defines configuration for a surveyor that check whether value is in a give list of options.
 *
 * @property options    available options
 * @property caseSensitive  true if the "contains" operation treats input literally, otherwise false; true by default
 * @author  Chaojun Sun
 * @since   0.1
 */
class OptionsRuleConfig : RuleConfig() {

    companion object {
        const val OPTIONS_RULE_CONFIG = "com.hagoapp.options"
    }

    var options = mutableListOf<String?>()
    var caseSensitive: Boolean = true

    override fun getConfigType(): String {
        return OPTIONS_RULE_CONFIG
    }
}
