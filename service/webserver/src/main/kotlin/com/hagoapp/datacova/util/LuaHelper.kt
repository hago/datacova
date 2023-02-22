/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.util

import com.hagoapp.datacova.CoVaException
import org.luaj.vm2.LuaValue
import java.time.LocalDateTime
import java.time.ZoneId

class LuaHelper {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun mapToLuaTable(input: Map<String, Any?>): LuaValue {
            val l = mutableListOf<LuaValue>()
            input.forEach { entry ->
                l.add(simpleToLuaValue(entry.key))
                when (entry.value) {
                    null -> LuaValue.NIL
                    is Map<*, *> -> mapToLuaTable(entry.value as Map<String, Any?>)
                    is List<*> -> listToLuaArray(entry.value as List<Any?>)
                    else -> l.add(simpleToLuaValue(entry.value))
                }
            }
            return LuaValue.tableOf(l.toTypedArray())
        }

        @Suppress("UNCHECKED_CAST")
        fun listToLuaArray(input: List<Any?>): LuaValue {
            val luaValues = input.map {
                when (it) {
                    null -> LuaValue.NIL
                    is Map<*, *> -> mapToLuaTable(it as Map<String, Any?>)
                    is List<*> -> listToLuaArray(it as List<Any?>)
                    else -> simpleToLuaValue(it)
                }
            }
            return LuaValue.listOf(luaValues.toTypedArray())
        }

        fun <T> simpleToLuaValue(input: T?): LuaValue {
            return when (input) {
                null -> LuaValue.NIL
                is String -> LuaValue.valueOf(input)
                is Boolean -> LuaValue.valueOf(input)
                is Int -> LuaValue.valueOf(input)
                is Double -> LuaValue.valueOf(input)
                is Float -> LuaValue.valueOf((input).toDouble())
                is ByteArray -> LuaValue.valueOf(input)
                is LocalDateTime -> LuaValue.valueOf(
                    input.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli().toString()
                )
                else -> throw CoVaException("Converting $input to lua value is not supported")
            }
        }
    }
}
