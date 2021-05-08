/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor.validator

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.verification.Configuration
import com.hagoapp.datacova.entity.action.verification.conf.RegexConfig
import com.hagoapp.datacova.execution.Validator
import com.hagoapp.f2t.DataRow
import java.util.regex.Pattern

class RegexValidator : Validator() {

    private lateinit var verifyFunc: (String?) -> Boolean

    override fun setConfig(configuration: Configuration?): Validator {
        return super.setConfig(configuration)
    }

    override fun prepare() {
        if (config !is RegexConfig) {
            throw CoVaException("Not a valid regex validator config")
        }
        val configuration = config!! as RegexConfig
        var flag = 0
        if (configuration.isDotAll) {
            flag += Pattern.DOTALL
        }
        if (configuration.isIgnoreCase) {
            flag += Pattern.CASE_INSENSITIVE
        }
        val pattern = Pattern.compile(configuration.pattern, flag)
        verifyFunc = { text ->
            when {
                text == null -> configuration.isNullable
                text.isBlank() -> configuration.isAllowEmpty
                else -> pattern.matcher(text).matches()
            }
        }
    }

    override fun getSupportedVerificationType(): Int {
        return RegexConfig.REGEX_CONFIGURATION_TYPE
    }

    override fun verify(row: DataRow): Map<String, Any?> {
        return fieldLoader.loadField(row).filter { item ->
            !verifyFunc(item.value.data?.toString())
        }.map { Pair(it.key, it.value.data) }.toMap()
    }
}
