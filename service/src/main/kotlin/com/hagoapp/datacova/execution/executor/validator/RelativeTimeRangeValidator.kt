/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor.validator

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.verification.conf.RelativeTimeRangeConfig
import com.hagoapp.datacova.execution.Validator
import com.hagoapp.f2t.DataRow
import com.hagoapp.f2t.util.JDBCTypeUtils
import java.sql.JDBCType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class RelativeTimeRangeValidator : Validator() {

    private lateinit var verifyFunc: (Long?) -> Pair<Boolean, String?>
    private lateinit var conf: RelativeTimeRangeConfig

    override fun prepare() {
        if (config !is RelativeTimeRangeConfig) {
            throw CoVaException("Not a valid relative time range config")
        }
        conf = (config as RelativeTimeRangeConfig)
        conf.lowerBound?.calculateValue()
        conf.upperBound?.calculateValue()
        verifyFunc = when {
            conf.lowerBound != null && conf.upperBound != null -> verifyBoth
            conf.lowerBound != null -> verifyLower
            conf.upperBound != null -> verifyUpper
            else -> { _ -> Pair(false, null) }
        }
    }

    private val verifyBoth = { num: Long? ->
        when (num) {
            null -> Pair(conf.isNullable, null)
            else -> Pair(
                isLessThan(conf.lowerBound.value, num, conf.lowerBound.isInclusive) &&
                        isLessThan(num, conf.upperBound.value, conf.upperBound.isInclusive),
                ZonedDateTime.ofInstant(Instant.ofEpochMilli(num), ZoneId.systemDefault()).toString()
            )
        }
    }

    private val verifyLower = { num: Long? ->
        if (num == null) Pair(conf.isNullable, null)
        else Pair(
            isLessThan(conf.lowerBound.value, num, conf.lowerBound.isInclusive),
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(num), ZoneId.systemDefault()).toString()
        )
    }

    private val verifyUpper = { num: Long? ->
        if (num == null) Pair(conf.isNullable, null)
        else Pair(
            isLessThan(num, conf.upperBound.value, conf.upperBound.isInclusive),
            ZonedDateTime.ofInstant(Instant.ofEpochMilli(num), ZoneId.systemDefault()).toString()
        )
    }

    private fun isLessThan(a: Long, b: Long, equable: Boolean): Boolean {
        return when {
            a < b -> true
            a > b -> false
            else -> equable
        }
    }

    override fun getSupportedVerificationType(): Int {
        return RelativeTimeRangeConfig.RELATIVE_TIME_RANGE_CONFIGURATION_TYPE
    }

    override fun verify(row: DataRow): Map<String, Any?> {
        return fieldLoader.loadField(row).map { item ->
            val v: Long? = when (item.value.data) {
                null -> null
                is ZonedDateTime -> (item.value.data as ZonedDateTime).toInstant().toEpochMilli()
                else -> (JDBCTypeUtils.toTypedValue(item.value.data, JDBCType.TIMESTAMP_WITH_TIMEZONE)
                        as ZonedDateTime?)?.toInstant()?.toEpochMilli()
            }
            val r = verifyFunc(v)
            Triple(item.key, r.first, r.second)
        }.filter { !it.second }.associate { Pair(it.first, it.third) }
    }
}
