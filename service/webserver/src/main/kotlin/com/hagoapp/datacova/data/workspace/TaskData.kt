/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.hagoapp.datacova.utility.MapSerializer
import com.hagoapp.datacova.lib.data.CoVaDatabase
import com.hagoapp.datacova.lib.action.TaskAction
import com.hagoapp.datacova.lib.action.TaskActionFactory
import com.hagoapp.datacova.lib.task.Task
import com.hagoapp.datacova.lib.task.TaskExtra
import com.hagoapp.datacova.lib.data.DatabaseConfig
import com.hagoapp.datacova.lib.data.DatabaseFunctions
import java.sql.ResultSet
import java.sql.Timestamp

class TaskData(config: DatabaseConfig) : CoVaDatabase(config) {

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
            id = rs.getInt("id")
            name = rs.getString("name")
            description = rs.getString("description")
            actions = Task.actionsFromJson(rs.getString("actions"))
            extra = TaskExtra.fromJson(rs.getString("extra"))
            addBy = rs.getLong("addby")
            addTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            modifyBy = DatabaseFunctions.getDBValue(rs, "modifyby")
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
        val sql = "select * from task where wkid = ? order by modifytime desc, addtime desc"
        connection.prepareStatement(sql).use { stmt ->
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

    fun createTask(task: Task): Task {
        val sql = "insert into task (name, description, wkid, actions, extra, addby, modifyby, modifytime) " +
                "values(?, ?, ?, ?, ?, ?, ?, now()) returning id"
        val id = connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, task.name)
            stmt.setString(2, task.description)
            stmt.setInt(3, task.workspaceId)
            stmt.setObject(4, DatabaseFunctions.createPgObject("json", task.actions))
            stmt.setObject(5, DatabaseFunctions.createPgObject("json", task.extra))
            stmt.setLong(6, task.addBy)
            stmt.setLong(7, task.addBy)
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        return loadTask(id)!!
    }

    fun updateTask(task: Task): Task {
        val sql = "update task set name = ?, description = ?, actions = ?, extra = ?, " +
                "modifyby = ?, modifytime = now() where id = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, task.name)
            stmt.setString(2, task.description)
            stmt.setObject(3, DatabaseFunctions.createPgObject("json", task.actions))
            stmt.setObject(4, DatabaseFunctions.createPgObject("json", task.extra))
            stmt.setLong(5, task.modifyBy)
            stmt.setInt(6, task.id)
            stmt.execute()
        }
        return loadTask(task.id)!!
    }

    fun deleteTask(taskId: Int) {
        connection.prepareStatement("delete from task where id = ?").use { stmt ->
            stmt.setInt(1, taskId)
            stmt.execute()
        }
    }
}
