/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.executor.validator

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.entity.action.verification.conf.LuaScriptConfig
import com.hagoapp.datacova.execution.Validator
import com.hagoapp.datacova.util.LuaHelper
import com.hagoapp.f2t.DataRow
import org.luaj.vm2.LuaBoolean
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.JsePlatform

class LuaScriptValidator : Validator() {
    private lateinit var luaValidator: LuaValue
    private lateinit var conf: LuaScriptConfig

    override fun getSupportedVerificationType(): Int {
        return LuaScriptConfig.LUA_SCRIPT_CONFIGURATION_TYPE
    }

    override fun prepare() {
        if (config !is LuaScriptConfig) {
            throw CoVaException("Not a valid Lua script config")
        }
        conf = config as LuaScriptConfig
        luaValidator = JsePlatform.standardGlobals().load(conf.snippet)
    }

    override fun verify(row: DataRow): Map<String, Any?> {
        val fields = fieldLoader.loadField(row).map { Pair(it.key, it.value.data) }.toMap()
        if (fields.size < conf.fields.size) {
            throw CoVaException("No enough fields, ${conf.fields.size} fields required for each row")
        }
        val luaParam = LuaHelper.mapToLuaTable(fields)
        val luaRet = luaValidator.call(luaParam)
        return if ((luaRet !is LuaBoolean) || !luaRet.booleanValue()) fields else mapOf()
    }

}
