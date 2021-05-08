/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor.validator

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.verification.conf.OptionsConfig
import com.hagoapp.datacova.execution.Validator
import com.hagoapp.f2t.DataRow

class OptionValidator : Validator() {

    private lateinit var verifyFunc: (String?) -> Boolean

    override fun getSupportedVerificationType(): Int {
        return OptionsConfig.OPTIONS_CONFIG_TYPE
    }

    override fun verify(row: DataRow): Map<String, Any?> {
        return fieldLoader.loadField(row).filter { item ->
            !verifyFunc(item.value.data?.toString())
        }.map { Pair(it.key, it.value.data) }.toMap()
    }

    override fun prepare() {
        if (config !is OptionsConfig) {
            throw CoVaException("Not a valid options validator config")
        }
        val conf: OptionsConfig = (config as OptionsConfig)
        verifyFunc = when {
            conf.isIgnoreCase && conf.isAllowEmpty -> { opt ->
                when {
                    opt == null -> true
                    opt.isBlank() -> true
                    else -> conf.options.any { it.equals(opt, true) }
                }
            }
            conf.isIgnoreCase -> { opt -> conf.options.any { it.equals(opt, true) } }
            conf.isAllowEmpty -> { opt ->
                if ((opt == null) || opt.isBlank()) true else conf.options.contains(opt)
            }
            else -> { opt -> conf.options.contains(opt) }
        }
    }
}
