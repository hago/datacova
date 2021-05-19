/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.workspace

import com.hagoapp.datacova.data.execution.TaskExecutionCache
import com.hagoapp.datacova.data.execution.TaskExecutionData
import com.hagoapp.datacova.data.workspace.*
import com.hagoapp.datacova.entity.execution.ExecutionFileInfo
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.entity.task.Task
import com.hagoapp.datacova.util.FileStoreUtils
import com.hagoapp.datacova.util.WorkspaceUserRoleUtil
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import com.hagoapp.f2t.datafile.FileInfoReader
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class Tasks {

    @WebEndPoint(
        path = "/api/workspace/:id/tasks",
        methods = [HttpMethod.GET],
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
        methods = [HttpMethod.PUT],
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
        methods = [HttpMethod.DELETE],
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
        methods = [HttpMethod.GET],
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
        methods = [HttpMethod.POST],
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
        val filestore = FileStoreUtils.getUploadedFileStore()
        val exec = TaskExecutionData().use { db ->
            val target = filestore.copyFileToStore(file.uploadedFileName())
            val eai = ExecutionFileInfo()
            val fi = FileInfoReader.json2FileInfo(rawInfo)
            fi.filename = filestore.getRelativeFileName(target.absoluteFileName)
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
}
