/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor.validator

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.verification.conf.NumberRangeConfig
import com.hagoapp.datacova.execution.Validator
import com.hagoapp.f2t.DataRow
import java.util.*

class NumberRangeValidator : Validator() {
    private lateinit var verifyFunc: (Double?) -> Boolean

    override fun prepare() {
        if (config !is NumberRangeConfig) {
            throw CoVaException("Not a valid number range config")
        }
        val conf = (config as NumberRangeConfig)
        verifyFunc = when {
            conf.lowerBound != null && conf.upperBound != null -> { num ->
                when (num) {
                    null -> conf.isNullable
                    else -> isLessThan(conf.lowerBound.value, num, conf.lowerBound.isInclusive) &&
                            isLessThan(num, conf.upperBound.value, conf.upperBound.isInclusive)
                }
            }
            conf.lowerBound != null -> { num ->
                if (num == null) conf.isNullable
                else isLessThan(conf.lowerBound.value, num, conf.lowerBound.isInclusive)
            }
            conf.upperBound != null -> { num ->
                if (num == null) conf.isNullable
                else isLessThan(num, conf.upperBound.value, conf.upperBound.isInclusive)
            }
            else -> { _ -> false }
        }
    }

    private fun isLessThan(a: Double, b: Double, equable: Boolean): Boolean {
        return when {
            a < b -> true
            a > b -> false
            else -> equable
        }
    }

    override fun getSupportedVerificationType(): Int {
        return NumberRangeConfig.CONFIGURATION_NUMBER_RANGE_TYPE
    }

    override fun verify(row: DataRow): Map<String, Any?> {
        return fieldLoader.loadField(row).filter { item ->
            val value = item.value.data?.toString()?.toDouble()
            !verifyFunc(value)
        }.map { Pair(it.key, it.value.data) }.toMap()
    }
}
