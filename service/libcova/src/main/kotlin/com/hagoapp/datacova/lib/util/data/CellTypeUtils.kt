/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.lib.util.data

import org.apache.poi.ss.format.CellFormatType
import org.apache.poi.ss.usermodel.CellType
import java.sql.JDBCType

class CellTypeUtils {
    companion object {
        fun jDBCTypeToCellType(jType: JDBCType): Pair<CellType, CellFormatType> {
            return when (jType) {
                JDBCType.BOOLEAN -> Pair(CellType.BOOLEAN, CellFormatType.GENERAL)
                JDBCType.INTEGER, JDBCType.BIGINT, JDBCType.DOUBLE, JDBCType.DECIMAL, JDBCType.FLOAT -> Pair(
                    CellType.NUMERIC,
                    CellFormatType.NUMBER
                )
                JDBCType.TIMESTAMP, JDBCType.DATE, JDBCType.TIME, JDBCType.TIMESTAMP_WITH_TIMEZONE, JDBCType.TIME_WITH_TIMEZONE -> Pair(
                    CellType.STRING,
                    CellFormatType.DATE
                )
                else -> Pair(CellType.STRING, CellFormatType.TEXT)
            }
        }
    }
}