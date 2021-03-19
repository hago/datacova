/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.hagoapp.datacova.MapSerializer
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.entity.execution.ExecutionDetail
import com.hagoapp.datacova.entity.execution.ExecutionFileInfo
import com.hagoapp.datacova.entity.execution.ExecutionStatus
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.entity.task.Task
import com.hagoapp.datacova.entity.action.TaskAction
import com.hagoapp.datacova.entity.action.TaskActionFactory
import com.hagoapp.datacova.entity.task.TaskExtra
import com.hagoapp.datacova.util.data.DatabaseFunctions
import java.sql.ResultSet
import java.sql.Timestamp

class TaskData(config: DatabaseConfig) : CoVaDatabase(config) {

    constructor() : this(CoVaConfig.getConfig().database)

    fun loadTask(id: Int): Task? {
        val sql = "select * from task where id = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) row2Task(rs) else null
            }
        }
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
            modifyBy = DatabaseFunctions.getDBValue<Timestamp>(rs, "modifyby")?.toInstant()?.toEpochMilli()
            modifyTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "modifytime")?.toInstant()?.toEpochMilli()
            workspaceId = rs.getInt("wkid")
            actions = loadTaskAction(rs.getString("actions"))
        }
        return task
    }

    private fun loadTaskAction(s: String): List<TaskAction> {
        val lm = MapSerializer.deserializeList(s)
        return lm.map { map ->
            TaskActionFactory.getTaskAction(map)
        }
    }

    fun getTasks(workspaceId: Int): List<Task> {
        connection.prepareStatement("select * from task where wkid = ?").use { stmt ->
            stmt.setInt(1, workspaceId)
            val tasks = mutableListOf<Task>()
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    tasks.add(row2Task(rs))
                }
            }
            return tasks
        }
    }

    fun loadTaskExecution(id: Long): TaskExecution? {
        connection.prepareStatement("select * from taskexecution where id = ?").use { stmt ->
            stmt.setLong(1, id)
            stmt.executeQuery().use { rs ->
                return if (rs.next()) row2taskExecution(rs) else null
            }
        }
    }

    private fun row2taskExecution(rs: ResultSet): TaskExecution {
        val te = TaskExecution()
        with(te) {
            id = rs.getLong("id")
            addBy = rs.getString("addby")
            addTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            startTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "starttime")?.toInstant()?.toEpochMilli()
            endTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "endtime")?.toInstant()?.toEpochMilli()
            status = ExecutionStatus.valueOf(rs.getInt("xstatus"))
            detail = ExecutionDetail.fromString(rs.getString("detail"))
            fileInfo = ExecutionFileInfo.getFileInfo(rs.getString("fileinfo"))
            task = Task.fromJson(rs.getString("task"))
            taskId = task.id
        }
        return te
    }

    fun createTask(task: Task): Task {
        val sql = "insert into task (name, description, wkid, actions, extra, addby) " +
                "values(?, ?, ?, ?, ?, ?) returning id"
        val id = connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, task.name)
            stmt.setString(2, task.description)
            stmt.setInt(3, task.workspaceId)
            stmt.setObject(4, DatabaseFunctions.createPgObject("json", task.actions))
            stmt.setObject(5, DatabaseFunctions.createPgObject("json", task.extra))
            stmt.setLong(6, task.addBy)
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        return loadTask(id)!!
    }

    fun updateTask(task: Task): Task {
        return task;
    }
}
