package com.hagoapp.datacova.web.workspace

import com.google.gson.GsonBuilder
import com.hagoapp.datacova.data.execution.TaskExecutionCache
import com.hagoapp.datacova.data.execution.TaskExecutionCache.Companion.TASK_EXECUTION_DEFAULT_PAGE_SIZE
import com.hagoapp.datacova.data.execution.TaskExecutionData
import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.data.workspace.WorkspaceCache
import com.hagoapp.datacova.data.workspace.WorkSpaceData
import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.util.WorkspaceUserRoleUtil
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class WorkSpaceApi {

    @WebEndPoint(
        methods = [HttpMethod.GET],
        path = "/api/workspace/mine",
        authTypes = [AuthType.UserToken]
    )
    fun myWorkSpaces(routeContext: RoutingContext) {
        val user = Authenticator.getUser(routeContext)
        val workspaces = getMyWorkSpaces(user.id)
        ResponseHelper.sendResponse(
            routeContext, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to workspaces
            )
        )
    }

    private fun getMyWorkSpaces(userId: Long): List<WorkspaceWithUser> {
        val workspaces = WorkspaceCache.getMyWorkSpaces(userId)
        return workspaces?.map {
            WorkspaceWithUser(
                workspace = it,
                users = findWorkspaceUsers(it.id),
                owner = UserCache.getUser(it.ownerId)!!
            )
        } ?: listOf()
    }

    private fun findWorkspaceUsers(workspaceId: Int): List<WorkspaceUser> {
        val list = WorkspaceCache.getWorkspaceUserInRoles(workspaceId)
        val userInfoList = list.mapNotNull { UserCache.getUser(it.userid) }
        return userInfoList.map { u ->
            WorkspaceUser(
                user = u,
                roles = list.filter { it.userid == u.id }.map { it.role }
            )
        }
    }

    @WebEndPoint(
        methods = [HttpMethod.PUT, HttpMethod.POST],
        path = "/api/workspace/add",
        authTypes = [AuthType.UserToken]
    )
    fun addWorkSpace(routeContext: RoutingContext) {
        val userInfo = Authenticator.getUser(routeContext)
        val wk = parseWorkSpace(routeContext)
        if (wk == null) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.BAD_REQUEST, "Invalid Workspace Data")
            return
        }
        wk.ownerId = userInfo.id
        wk.addBy = userInfo.id
        val workSpace = WorkSpaceData().addWorkSpace(wk)
        WorkspaceCache.clearMyWorkspaces(userInfo.id)
        if (workSpace == null) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.CONFLICT, "duplicated name")
        } else {
            ResponseHelper.sendResponse(routeContext, HttpResponseStatus.OK, mapOf("code" to 0, "data" to workSpace))
        }
    }

    private fun parseWorkSpace(routeContext: RoutingContext): WorkSpace? {
        return try {
            val load = routeContext.bodyAsString
            GsonBuilder().create().fromJson(load, WorkSpace::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    @WebEndPoint(
        methods = [HttpMethod.PUT],
        path = "/api/workspace/update",
        authTypes = [AuthType.UserToken]
    )
    fun updateWorkSpace(routeContext: RoutingContext) {
        val wk = parseWorkSpace(routeContext)
        if (wk == null) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.BAD_REQUEST, "Invalid Workspace Data")
            return
        }
        val user = Authenticator.getUser(routeContext)
        val workspace = WorkspaceCache.getWorkspace(wk.id)
        if ((workspace == null) || !WorkspaceUserRoleUtil.isAdmin(user, workspace)) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.NOT_FOUND, "WorkSpace ${wk.id} not found")
            return
        }
        wk.modifyBy = user.id
        val workspace0 = WorkSpaceData().updateWorkSpace(wk)
        WorkspaceCache.clearMyWorkspaces(user.id)
        ResponseHelper.sendResponse(routeContext, HttpResponseStatus.OK, mapOf("code" to 0, "data" to workspace0))
    }

    @WebEndPoint(
        methods = [HttpMethod.GET],
        path = "/api/workspace/:id/executions",
        authTypes = [AuthType.UserToken]
    )
    fun getExecutionList(context: RoutingContext) {
        val id = context.pathParam("id").toInt()
        doGetExecutionRage(context, id, 0, TASK_EXECUTION_DEFAULT_PAGE_SIZE)
    }

    @WebEndPoint(
        methods = [HttpMethod.GET],
        path = "/api/workspace/:id/executions/:size",
        authTypes = [AuthType.UserToken]
    )
    fun getExecutionSizedList(context: RoutingContext) {
        val id = context.pathParam("id").toInt()
        val size = context.pathParam("size").toInt()
        doGetExecutionRage(context, id, 0, size)
    }

    @WebEndPoint(
        methods = [HttpMethod.GET],
        path = "/api/workspace/:id/executions/:start/:size",
        authTypes = [AuthType.UserToken]
    )
    fun getExecutionRange(context: RoutingContext) {
        val id = context.pathParam("id").toInt()
        val size = context.pathParam("size").toInt()
        val start = context.pathParam("start").toInt()
        doGetExecutionRage(context, id, start, size)
    }

    private fun doGetExecutionRage(context: RoutingContext, workspaceId: Int, start: Int, size: Int) {
        val user = Authenticator.getUser(context)
        if (!WorkspaceUserRoleUtil.isUser(user, workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "access denied")
            return
        }
        val executions = TaskExecutionCache.getExecutionsOfWorkspace(workspaceId, start, size)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to mapOf("executions" to executions)
            )
        )
    }
}
