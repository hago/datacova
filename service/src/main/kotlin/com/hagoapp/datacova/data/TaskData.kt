package com.hagoapp.datacova.data

import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.entity.execution.ExecutionDetail
import com.hagoapp.datacova.entity.execution.ExecutionFileInfo
import com.hagoapp.datacova.entity.execution.ExecutionStatus
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.entity.task.Task
import com.hagoapp.datacova.entity.task.TaskExtra
import com.hagoapp.datacova.util.data.getDBValue
import java.sql.ResultSet
import java.sql.Timestamp

class TaskData(config: DatabaseConfig) : CoVaDatabase(config) {

    fun loadTask(id: Long): Task? {
        val sql = "select * from task where id = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, id)
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
            addBy = rs.getString("addby")
            addTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            modifyBy = getDBValue(rs, "modifyby")
            modifyTime = getDBValue<Timestamp>(rs, "modifytime")?.toInstant()?.toEpochMilli()
            workspaceId = rs.getInt("wkid")
        }
        return task
    }

    fun getTasks(workspaceId: Int): List<Task> {
        connection.prepareStatement("select * from tasks where wkid = ?").use { stmt ->
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
            startTime = getDBValue<Timestamp>(rs, "starttime")?.toInstant()?.toEpochMilli()
            endTime = getDBValue<Timestamp>(rs, "endtime")?.toInstant()?.toEpochMilli()
            status = ExecutionStatus.valueOf(rs.getInt("xstatus"))
            detail = ExecutionDetail.fromString(rs.getString("detail"))
            fileInfo = ExecutionFileInfo.getFileInfo(rs.getString("fileinfo"))
            task = Task.fromJson(rs.getString("task"))
            taskId = task.id
        }
        return te
    }
}
