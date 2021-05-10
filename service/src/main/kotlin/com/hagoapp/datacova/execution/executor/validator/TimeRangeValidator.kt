/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.executor.validator

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.verification.conf.TimeRangeConfig
import com.hagoapp.datacova.execution.Validator
import com.hagoapp.f2t.DataRow
import com.hagoapp.f2t.util.JDBCTypeUtils
import java.sql.JDBCType
import java.time.ZonedDateTime

class TimeRangeValidator : Validator() {

    private lateinit var verifyFunc: (Long?) -> Boolean

    override fun prepare() {
        if (config !is TimeRangeConfig) {
            throw CoVaException("Not a valid number range config")
        }
        val conf = (config as TimeRangeConfig)
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

    private fun isLessThan(a: Long, b: Long, equable: Boolean): Boolean {
        return when {
            a < b -> true
            a > b -> false
            else -> equable
        }
    }

    override fun getSupportedVerificationType(): Int {
        return TimeRangeConfig.TIME_RANGE_CONFIGURATION_TYPE
    }

    override fun verify(row: DataRow): Map<String, Any?> {
        return fieldLoader.loadField(row).filter { item ->
            val v: Long? = when (item.value.data) {
                null -> null
                is ZonedDateTime -> (item.value.data as ZonedDateTime).toInstant().toEpochMilli()
                else -> (JDBCTypeUtils.toTypedValue(item.value.data, JDBCType.TIMESTAMP_WITH_TIMEZONE)
                        as ZonedDateTime?)?.toInstant()?.toEpochMilli()
            }
            !verifyFunc(v)
        }.map { Pair(it.key, it.value.data) }.toMap()
    }
}
