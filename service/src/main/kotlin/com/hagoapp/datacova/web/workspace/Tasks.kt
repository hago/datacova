/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.workspace

import com.hagoapp.datacova.data.execution.TaskExecutionCache
import com.hagoapp.datacova.data.execution.TaskExecutionData
import com.hagoapp.datacova.data.workspace.TaskCache
import com.hagoapp.datacova.data.workspace.TaskData
import com.hagoapp.datacova.data.workspace.WorkspaceCache
import com.hagoapp.datacova.dispatcher.Dispatcher
import com.hagoapp.datacova.entity.execution.ExecutionFileInfo
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.entity.task.Task
import com.hagoapp.datacova.util.FileStoreUtils
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
import java.sql.JDBCType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class Tasks {

    @WebEndPoint(
        path = "/api/workspace/:id/tasks",
        methods = [MethodName.GET],
        authTypes = [AuthType.UserToken]
    )
    fun listTasksOfWorkspace(context: RoutingContext) {
        val id = context.pathParam("id").toIntOrNull()
        if (id == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid workspace")
            return
        }
        val user = Authenticator.getUser(context)
        if (!WorkspaceUserRoleUtil.isUser(user, id)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "access denied")
            return
        }
        val l = TaskCache.listTasks(id)
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0, "data" to mapOf("tasks" to l)))
    }

    @WebEndPoint(
        path = "/api/workspace/:id/task/update",
        methods = [MethodName.PUT],
        authTypes = [AuthType.UserToken]
    )
    fun updateTask(context: RoutingContext) {
        val workspace = WorkspaceCache.getWorkspace(context.pathParam("id").toInt())
        val user = Authenticator.getUser(context)
        if (workspace == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid workspace")
            return
        }
        if (!WorkspaceUserRoleUtil.isAdmin(user, workspace) && !WorkspaceUserRoleUtil.isMaintainer(user, workspace)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "access denied")
            return
        }
        val rawTask = Task.fromJson(context.bodyAsString)
        val task0 = TaskCache.listTasks(workspace.id).firstOrNull { it.id == rawTask.id }
        val task = if (task0 == null) {
            rawTask.addBy = user.id
            TaskData().createTask(rawTask)
        } else {
            rawTask.modifyBy = user.id
            TaskData().updateTask(rawTask)
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
        authTypes = [AuthType.UserToken]
    )
    fun deleteTask(context: RoutingContext) {
        val workspaceId = context.pathParam("wkid").toInt()
        val id = context.pathParam("id").toInt()
        val workspace = WorkspaceCache.getWorkspace(workspaceId)
        if (workspace == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid workspace")
            return
        }
        val task = TaskCache.listTasks(workspaceId).firstOrNull { t -> t.id == id }
        if ((task == null) || (task.workspaceId != workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid connection")
            return
        }
        val user = Authenticator.getUser(context)
        if (!WorkspaceUserRoleUtil.isAdmin(user, workspace) && !WorkspaceUserRoleUtil.isMaintainer(user, workspace)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "access denied")
            return
        }
        TaskData().deleteTask(id)
        TaskCache.clearWorkspaceTasks(workspaceId)
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0))
    }

    @WebEndPoint(
        path = "/api/workspace/:wkid/task/:id",
        methods = [MethodName.GET],
        authTypes = [AuthType.UserToken]
    )
    fun getTask(context: RoutingContext) {
        val workspaceId = context.pathParam("wkid").toInt()
        val id = context.pathParam("id").toInt()
        val workspace = WorkspaceCache.getWorkspace(workspaceId)
        if (workspace == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid workspace")
            return
        }
        val user = Authenticator.getUser(context)
        if (!WorkspaceUserRoleUtil.isUser(user, workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "access denied")
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
        authTypes = [AuthType.UserToken]
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
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "access denied")
            return
        }
        val taskId = context.pathParam("id").toInt()
        val task = TaskCache.listTasks(workspaceId).firstOrNull { it.id == taskId }
        if ((task == null)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "no such task")
            return
        }
        val rawInfo = context.request().getParam("extra")
        val fileStore = FileStoreUtils.getUploadedFileStore()
        val exec = TaskExecutionData().use { db ->
            val target = fileStore.copyFileToStore(file.uploadedFileName())
            val eai = ExecutionFileInfo()
            val fi = FileInfoReader.json2FileInfo(rawInfo)
            fi.filename = fileStore.getRelativeFileName(target.absoluteFileName)
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
        Dispatcher.get().executionComing(exec)
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
        authTypes = [AuthType.UserToken]
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
        val info = if (fi is FileInfoExcel) ExcelDataFileParser(file.uploadedFileName()).getInfo() else null
        ReaderFactory.getReader(fi).use { reader ->
            val size = context.request().getParam("size").toIntOrNull() ?: 20
            val start = context.request().getParam("start").toIntOrNull() ?: 0
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
            ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to mapOf(
                    "columns" to cols.map { it.name },
                    "rows" to rows.map { row ->
                        row.cells.map { formatData(it.data, cols[it.index].dataType) }
                    },
                    "info" to info
                )
            ))
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
            else -> JDBCTypeUtils.toTypedValue(data, type)
        }

    }
}
