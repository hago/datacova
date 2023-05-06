/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.data.execution

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.entity.execution.ExecutionDetail
import com.hagoapp.datacova.entity.execution.ExecutionFileInfo
import com.hagoapp.datacova.entity.execution.ExecutionStatus
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.entity.task.Task
import com.hagoapp.datacova.util.data.DatabaseFunctions
import java.sql.ResultSet
import java.sql.Timestamp

class TaskExecutionData(config: DatabaseConfig) : CoVaDatabase(config) {
    constructor() : this(CoVaConfig.getConfig().database)

    fun createTaskExecution(taskExec: TaskExecution): TaskExecution {
        val sql = "insert into taskexecution (fileinfo, taskid, addby, task) values(?,?,?,?) returning id"
        val id = connection.prepareStatement(sql).use { stmt ->
            stmt.setObject(1, DatabaseFunctions.createPgObject("json", taskExec.fileInfo.toJson()))
            stmt.setInt(2, taskExec.taskId)
            stmt.setLong(3, taskExec.addBy)
            stmt.setObject(4, DatabaseFunctions.createPgObject("json", taskExec.task?.toJson()))
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        return getTaskExecution(id)!!
    }

    fun getTaskExecution(id: Int): TaskExecution? {
        val sql = "select * from taskexecution where id = ?"
        return connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                if (rs.next()) row2TaskExecution(rs)
                else null
            }
        }
    }

    private fun row2TaskExecution(rs: ResultSet): TaskExecution {
        val te = TaskExecution()
        with(te) {
            taskId = rs.getInt("taskid")
            addBy = rs.getLong("addby")
            addTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "addtime")!!.toInstant().toEpochMilli()
            startTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "starttime")?.toInstant()?.toEpochMilli()
            endTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "endtime")?.toInstant()?.toEpochMilli()
            status = ExecutionStatus.valueOf(rs.getInt("xstatus"))
            fileInfo = ExecutionFileInfo.getFileInfo(rs.getString("fileinfo"))
            detail = ExecutionDetail.fromString(rs.getString("detail"))
            task = Task.fromJson(rs.getString("task"))
            id = rs.getInt("id")
        }
        return te
    }

    fun getTaskExecutionsOfWorkspace(workspaceId: Int, start: Int, size: Int): List<TaskExecution> {
        val sql = "select * from taskexecution where task->>'workspaceId'=? order by id desc offset ? limit ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, workspaceId.toString())
            stmt.setInt(2, start)
            stmt.setInt(3, size)
            val executions = mutableListOf<TaskExecution>()
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    executions.add(row2TaskExecution(rs))
                }
            }
            return executions
        }
    }

    fun completeTaskExecution(detail: ExecutionDetail) {
        val sql = "update taskexecution set detail = ?, xstatus = ?, endtime = CURRENT_TIMESTAMP where id = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setObject(1, DatabaseFunctions.createPgObject("json", detail))
            stmt.setInt(2, if (detail.isSucceeded) 2 else -1)
            stmt.setInt(3, detail.execution.id)
            stmt.execute()
        }
    }

    fun startTaskExecution(te: TaskExecution) {
        val sql = "update taskexecution set xstatus = 1, starttime = CURRENT_TIMESTAMP, endtime = null where id = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, te.id)
            stmt.execute()
        }
    }

    fun loadQueueingTaskExecution(): List<TaskExecution> {
        val sql = "select * from taskexecution where xstatus = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, ExecutionStatus.added.value)
            val ret = mutableListOf<TaskExecution>()
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    ret.add(row2TaskExecution(rs))
                }
            }
            return ret
        }
    }
}
