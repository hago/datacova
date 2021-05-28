/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.rules

import com.hagoapp.datacova.MapSerializer
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.entity.action.verification.ConfigurationFactory
import com.hagoapp.datacova.entity.action.verification.Rule
import java.sql.ResultSet

class ValidationRuleData(config: DatabaseConfig) : CoVaDatabase(config) {
    constructor() : this(CoVaConfig.getConfig().database)

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
            ruleConfig = ConfigurationFactory.createConfiguration(map, json)
        }
        return rule
    }
}
