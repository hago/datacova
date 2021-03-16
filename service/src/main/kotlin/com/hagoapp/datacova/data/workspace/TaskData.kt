/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.entity.task.Task
import com.hagoapp.datacova.entity.task.TaskExtra
import com.hagoapp.datacova.util.data.DatabaseFunctions.Companion.getDBValue
import java.sql.ResultSet
import java.sql.Timestamp

class TaskData(config: DatabaseConfig): CoVaDatabase(config) {

    constructor() : this(CoVaConfig.getConfig().database)

    fun listWorkspaceTask(workspaceId: Int): List<Task> {
        val sql = "select * from task where wkid = ?"
        val l = mutableListOf<Task>()
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, workspaceId)
            stmt.executeQuery().use { rs ->
                l.add(row2Task(rs))
            }
        }
        return l
    }

    private fun row2Task(rs: ResultSet): Task {
        val task = Task()
        with(task) {
            id = rs.getLong("id")
            name = rs.getString("name")
            description = rs.getString("description")
            actions = Task.actionsFromJson(rs.getString("actions"))
            extra = TaskExtra.fromJson(rs.getString("extra"))
            addBy = rs.getLong("addby")
            addTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            modifyBy = getDBValue(rs, "modifyby")
            modifyTime = getDBValue<Timestamp>(rs, "modifytime")?.toInstant()?.toEpochMilli()
            workspaceId = rs.getInt("wkid")
        }
        return task
    }
}
