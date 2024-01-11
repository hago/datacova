/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.workspace

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.FileStorageConfig
import com.hagoapp.datacova.data.execution.TaskExecutionCache
import com.hagoapp.datacova.data.workspace.TaskCache
import com.hagoapp.datacova.data.workspace.TaskData
import com.hagoapp.datacova.data.workspace.WorkspaceCache
import com.hagoapp.datacova.lib.data.TaskExecutionData
import com.hagoapp.datacova.lib.execution.ExecutionFileInfo
import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.lib.task.Task
import com.hagoapp.datacova.util.WorkspaceUserRoleUtil
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import com.hagoapp.f2t.DataRow
import com.hagoapp.f2t.datafile.FileInfoReader
import com.hagoapp.f2t.datafile.ReaderFactory
import com.hagoapp.f2t.datafile.excel.ExcelDataFileParser
import com.hagoapp.f2t.datafile.excel.FileInfoExcel
import com.hagoapp.f2t.util.JDBCTypeUtils
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.sql.JDBCType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Tasks {

    companion object {
        private const val MESSAGE_ACCESS_DENIED = "access denied"
        private const val MESSAGE_INVALID_WORKSPACE = "invalid workspaced"
    }

    private val logger = LoggerFactory.getLogger(Tasks::class.java)

    @WebEndPoint(
        path = "/api/workspace/:id/tasks",
        methods = [MethodName.GET],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun listTasksOfWorkspace(context: RoutingContext) {
        val id = context.pathParam("id").toIntOrNull()
        if (id == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, MESSAGE_INVALID_WORKSPACE)
            return
        }
        val user = Authenticator.getUser(context)
        if (!WorkspaceUserRoleUtil.isUser(user, id)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, MESSAGE_ACCESS_DENIED)
            return
        }
        val l = TaskCache.listTasks(id)
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0, "data" to mapOf("tasks" to l)))
    }

    @WebEndPoint(
        path = "/api/workspace/:id/task/update",
        methods = [MethodName.PUT],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun updateTask(context: RoutingContext) {
        val workspace = WorkspaceCache.getWorkspace(context.pathParam("id").toInt())
        val user = Authenticator.getUser(context)
        if (workspace == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, MESSAGE_INVALID_WORKSPACE)
            return
        }
        if (!WorkspaceUserRoleUtil.isAdmin(user, workspace) && !WorkspaceUserRoleUtil.isMaintainer(user, workspace)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, MESSAGE_ACCESS_DENIED)
            return
        }
        val rawTask = Task.fromJson(context.body().asString())
        val task0 = TaskCache.listTasks(workspace.id).firstOrNull { it.id == rawTask.id }
        val task = if (task0 == null) {
            rawTask.addBy = user.id
            TaskData(CoVaConfig.getConfig().database).createTask(rawTask)
        } else {
            rawTask.modifyBy = user.id
            TaskData(CoVaConfig.getConfig().database).updateTask(rawTask)
        }
        TaskCache.clearWorkspaceTasks(workspace.id)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to task
            )
        )
    }

    @WebEndPoint(
        path = "/api/workspace/:wkid/task/:id",
        methods = [MethodName.DELETE],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun deleteTask(context: RoutingContext) {
        val workspaceId = context.pathParam("wkid").toInt()
        val id = context.pathParam("id").toInt()
        val workspace = WorkspaceCache.getWorkspace(workspaceId)
        if (workspace == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, MESSAGE_INVALID_WORKSPACE)
            return
        }
        val task = TaskCache.listTasks(workspaceId).firstOrNull { t -> t.id == id }
        if ((task == null) || (task.workspaceId != workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid connection")
            return
        }
        val user = Authenticator.getUser(context)
        if (!WorkspaceUserRoleUtil.isAdmin(user, workspace) && !WorkspaceUserRoleUtil.isMaintainer(user, workspace)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, MESSAGE_ACCESS_DENIED)
            return
        }
        TaskData(CoVaConfig.getConfig().database).deleteTask(id)
        TaskCache.clearWorkspaceTasks(workspaceId)
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0))
    }

    @WebEndPoint(
        path = "/api/workspace/:wkid/task/:id",
        methods = [MethodName.GET],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun getTask(context: RoutingContext) {
        val workspaceId = context.pathParam("wkid").toInt()
        val id = context.pathParam("id").toInt()
        val workspace = WorkspaceCache.getWorkspace(workspaceId)
        if (workspace == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, MESSAGE_INVALID_WORKSPACE)
            return
        }
        val user = Authenticator.getUser(context)
        if (!WorkspaceUserRoleUtil.isUser(user, workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, MESSAGE_ACCESS_DENIED)
            return
        }
        val task = TaskCache.listTasks(workspaceId).firstOrNull { t -> t.id == id }
        if ((task == null) || (task.workspaceId != workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid connection")
            return
        }
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0, "data" to task))
    }

    @WebEndPoint(
        path = "/api/workspace/:wkid/task/:id/run",
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun runTask(context: RoutingContext) {
        val files = context.fileUploads()
        if (files.isEmpty()) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "no file found")
            return
        }
        val file = context.fileUploads().first()
        val workspaceId = context.pathParam("wkid").toInt()
        val workSpace = WorkspaceCache.getWorkspace(workspaceId)
        val user = Authenticator.getUser(context)
        if ((workSpace == null) || !WorkspaceUserRoleUtil.isUser(user, workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, MESSAGE_ACCESS_DENIED)
            return
        }
        val taskId = context.pathParam("id").toInt()
        val task = TaskCache.listTasks(workspaceId).firstOrNull { it.id == taskId }
        if ((task == null)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "no such task")
            return
        }
        val rawInfo = context.request().getParam("extra")
        val fileStore = FileStorageConfig.createFileStore(CoVaConfig.getConfig().fileStorage.uploadFileStore)
        val exec = TaskExecutionData(CoVaConfig.getConfig().database).use { db ->
            val target = FileInputStream(file.uploadedFileName()).use {
                fileStore.putFile(it, file.fileName(), file.size())
            }
            val eai = ExecutionFileInfo()
            val fi = FileInfoReader.json2FileInfo(rawInfo)
            fi.filename = target
            with(eai) {
                originalName = file.fileName()
                size = file.size()
                fileInfo = fi
            }
            val execTask = TaskExecution()
            with(execTask) {
                this.taskId = taskId
                this.addBy = user.id
                fileInfo = eai
                this.task = task
            }
            db.createTaskExecution(execTask)
        }
        TaskExecutionCache.clearWorkspaceTaskExecutions(workspaceId)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to exec
            )
        )
    }

    @WebEndPoint(
        path = "/api/file/preview",
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun preview(context: RoutingContext) {
        val files = context.fileUploads()
        if (files.isEmpty()) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "no file found")
            return
        }
        val file = context.fileUploads().first()
        val rawInfo = context.request().getParam("extra")
        val fi = FileInfoReader.json2FileInfo(rawInfo)
        fi.filename = file.uploadedFileName()
        val info = if (fi is FileInfoExcel) ExcelDataFileParser(file.uploadedFileName()).excelInfo() else null
        ReaderFactory.getReader(fi).use { reader ->
            val size = context.request().getParam("size", "20").toInt()
            val start = context.request().getParam("start", "0").toInt()
            reader.open(fi)
            reader.findColumns()
            val cols = reader.inferColumnTypes(start.toLong() + size.toLong())
            var i = 0
            val end = start + size
            val rows = mutableListOf<DataRow>()
            while (reader.hasNext()) {
                val row = reader.next()
                if (i >= start) {
                    rows.add(row)
                }
                i++
                if (i >= end) {
                    break
                }
            }
            ResponseHelper.sendResponse(
                context, HttpResponseStatus.OK, mapOf(
                    "code" to 0,
                    "data" to mapOf(
                        "columns" to cols.sortedBy { it.order }.map { it.name },
                        "rows" to rows.map { row ->
                            row.cells.map { formatData(it.data, cols.first { col -> col.order == it.index }.dataType) }
                        },
                        "info" to info
                    )
                )
            )
        }
    }

    private fun formatData(data: Any?, type: JDBCType?): Any? {
        return when {
            data == null -> null
            type == null -> data.toString()
            type == JDBCType.TIMESTAMP || type == JDBCType.TIMESTAMP_WITH_TIMEZONE -> {
                val x = JDBCTypeUtils.toTypedValue(data, type) as ZonedDateTime
                DateTimeFormatter.ISO_DATE_TIME.format(x)
            }
            type == JDBCType.DATE -> {
                val x = JDBCTypeUtils.toTypedValue(data, type) as LocalDate
                DateTimeFormatter.ISO_DATE.format(x)
            }
            type == JDBCType.TIME || type == JDBCType.TIME_WITH_TIMEZONE -> {
                val x = JDBCTypeUtils.toTypedValue(data, type) as LocalTime
                DateTimeFormatter.ISO_TIME.format(x)
            }
            else -> {
                logger.debug("for data {} with type {}", data, type)
                JDBCTypeUtils.toTypedValue(data, type)
            }
        }

    }
}
