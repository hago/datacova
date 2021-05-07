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

    override fun withConfig(configuration: Configuration?): Validator {
        if (configuration !is RegexConfig) {
            throw CoVaException("Not a valid regex validator config")
        }
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
        return super.withConfig(configuration)
    }

    override fun getSupportedVerificationType(): Int {
        return RegexConfig.REGEX_CONFIGURATION_TYPE
    }

    override fun verify(row: DataRow): List<String> {
        return fieldLoader.loadField(row).filter { item ->
            !verifyFunc(item.value.data?.toString())
        }.map { it.key }
    }

    override fun getAbstract(): String {
        TODO("Not yet implemented")
    }
}
