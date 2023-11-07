/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util.data

import com.hagoapp.datacova.lib.data.DatabaseConfig
import com.hagoapp.datacova.utility.MapSerializer
import com.hagoapp.datacova.lib.verification.Rule
import com.hagoapp.datacova.lib.verification.VerifyConfiguration
import java.sql.ResultSet

class ValidationRuleData(config: DatabaseConfig) : CoVaDatabase(config) {

    fun getRules(workspaceId: Int, start: Int, size: Int): List<Rule> {
        val sql = "select * from rules where wkid = ? offset ? limit ?"
        val ret = mutableListOf<Rule>()
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, workspaceId)
            stmt.setInt(2, start)
            stmt.setInt(3, size)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    ret.add(row2Rule(rs))
                }
            }
        }
        return ret
    }

    private fun row2Rule(rs: ResultSet): Rule {
        val rule = Rule()
        val json = rs.getString("ruleconfig")
        val map = MapSerializer.deserializeMap(json)
        with(rule) {
            id = rs.getLong("id")
            name = rs.getString("name")
            description = rs.getString("description")
            workspaceId = rs.getInt("wkid")
            addBy = rs.getLong("addby")
            addTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            modifyBy = rs.getLong("addby")
            modifyTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            ruleConfig = VerifyConfiguration.create(map)
        }
        return rule
    }

    fun getRule(id: Long): Rule? {
        val sql = "select * from rules where id = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, id)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return row2Rule(rs)
                }
            }
        }
        return null
    }

    fun newRule(rule: Rule): Rule? {
        val sql = """insert into rules(name, description, ruleconfig, wkid, addby, modifyby, modifytime)
            values (?, ?, ?, ?, ?, ?, now()) returning id"""
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, rule.name)
            stmt.setString(2, rule.description)
            stmt.setObject(3, DatabaseFunctions.createPgObject("json", rule.ruleConfig))
            stmt.setInt(4, rule.workspaceId)
            stmt.setLong(5, rule.addBy)
            stmt.setLong(6, rule.modifyTime)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) {
                    getRule(rs.getLong("id"))
                } else {
                    null
                }
            }
        }
    }

    fun updateRule(rule: Rule): Rule? {
        val sql = """update rules set name = ?, description = ?, ruleconfig = ?, modifyby = ?, modifytime = now()
            where id = ?"""
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, rule.name)
            stmt.setString(2, rule.description)
            stmt.setObject(3, DatabaseFunctions.createPgObject("json", rule.ruleConfig))
            stmt.setLong(4, rule.modifyBy)
            stmt.setLong(5, rule.id)
            stmt.execute()
        }
        return getRule(rule.id)
    }
}
