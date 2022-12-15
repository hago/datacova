/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.surveyor

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.util.text.TextResourceManager
import com.hagoapp.surveyor.RuleConfig
import com.hagoapp.surveyor.rule.*
import org.stringtemplate.v4.ST
import java.lang.Exception
import java.util.Locale

abstract class RuleConfigDescriptor {

    companion object {

        fun create(config: RuleConfig): RuleConfigDescriptor {
            return when (config) {
                is RegexRuleConfig -> regexDescriptor
                is OptionsRuleConfig -> optionsDescriptor
                is EmbedJythonRuleConfig -> embedJythonDescriptor
                is NumberRangeRuleConfig -> numberRangeDescriptor
                is TimeRangeRuleConfig -> timeRangeDescriptor
                else -> throw Exception()
            }
        }

        private val regexDescriptor: RegexRuleConfigDescriptor = RegexRuleConfigDescriptor()
        private val optionsDescriptor: OptionsRuleConfigDescriptor = OptionsRuleConfigDescriptor()
        private val timeRangeDescriptor: TimerRangeRuleConfigDescriptor = TimerRangeRuleConfigDescriptor()
        private val numberRangeDescriptor: NumberRangeRuleConfigDescriptor = NumberRangeRuleConfigDescriptor()
        private val embedJythonDescriptor: EmbedJythonRuleConfigDescriptor = EmbedJythonRuleConfigDescriptor()
    }

    private val localizedTemplates = mutableMapOf<Locale, ST?>()
    protected abstract val templateDirName: String

    @JvmOverloads
    fun describe(config: RuleConfig, locale: Locale = Locale.getDefault()): String {
        val expect = getExpectActualRuleConfigType()
        val actual = config::class.java
        if (actual != expect) {
            throw CoVaException("config in $expect is expected, while $actual instance is given")
        }
        val template = localizedTemplates.compute(locale) { loc, st ->
            if (st != null) st else {
                val template = TextResourceManager.getManager().getString(loc)
                if (template != null) ST(template) else null
            }
        }
            ?: throw UnsupportedOperationException("template for ${config::class.java.canonicalName} with locale ${locale.displayName} not found")
        return doDescribe(template, config)
    }

    protected abstract fun getExpectActualRuleConfigType(): Class<out RuleConfig>

    protected abstract fun doDescribe(st: ST, config: RuleConfig): String
}
