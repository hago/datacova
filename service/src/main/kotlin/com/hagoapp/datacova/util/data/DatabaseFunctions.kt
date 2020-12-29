package com.hagoapp.datacova.util.data

import com.google.gson.Gson
import org.postgresql.util.PGobject
import java.sql.ResultSet
import java.sql.Timestamp

fun <T> getDBValue(rs: ResultSet, column: String): T? {
    val x = rs.getObject(column)
    return try {
        @Suppress("UNCHECKED_CAST")
        if (!rs.wasNull()) x as T else null
    } catch (ex: TypeCastException) {
        null
    }
}

fun createPgObject(type: String, value: Any?): PGobject {
    val pgObj = PGobject()
    pgObj.type = type
    pgObj.value = if (value is String) value else Gson().toJson(value)
    return pgObj
}

fun timeStamp2Epoch(ts: Timestamp?): Long? {
    return when (ts) {
        null -> null
        else -> ts.toInstant().toEpochMilli()
    }
}

fun escapePGLike(word: String): String {
    return word.replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
}
