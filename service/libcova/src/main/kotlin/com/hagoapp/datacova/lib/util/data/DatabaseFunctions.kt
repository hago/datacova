/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util.data

import com.google.gson.Gson
import org.postgresql.util.PGobject
import java.sql.ResultSet
import java.sql.Timestamp

class DatabaseFunctions {
    companion object {
        @JvmStatic
        fun <T> getDBValue(rs: ResultSet, column: String): T? {
            val x = rs.getObject(column)
            return try {
                @Suppress("UNCHECKED_CAST")
                if (!rs.wasNull()) x as T else null
            } catch (ex: TypeCastException) {
                null
            }
        }

        @JvmStatic
        fun createPgObject(type: String, value: Any?): PGobject {
            val pgObj = PGobject()
            pgObj.type = type
            pgObj.value = if (value is String) value else Gson().toJson(value)
            return pgObj
        }

        @JvmStatic
        fun timeStamp2Epoch(ts: Timestamp?): Long? {
            return when (ts) {
                null -> null
                else -> ts.toInstant().toEpochMilli()
            }
        }

        @JvmStatic
        fun escapePGLike(word: String): String {
            return word.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
        }
    }
}
