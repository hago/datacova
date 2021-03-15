package com.hagoapp.datacova.web.workspace

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.data.user.UserAccess
import com.hagoapp.datacova.data.user.WorkspaceCache
import com.hagoapp.datacova.data.workspace.WorkSpaceData
import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
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
                owner = UserAccess.getUser(it.ownerId)!!
            )
        } ?: listOf()
    }

    private fun findWorkspaceUsers(workspaceId: Int): List<WorkspaceUser> {
        val list = WorkspaceCache.getWorkspaceUserInRoles(workspaceId)
        val userInfoList = list.mapNotNull { UserAccess.getUser(it.userid) }
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
        if ((workspace == null) || (workspace.ownerId != wk.ownerId) ||
            WorkspaceCache.getWorkspaceUserInRoles(workspace.id, listOf(WorkSpaceUserRole.Admin))
                .none { it.userid == user.id }
        ) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.NOT_FOUND, "WorkSpace ${wk.id} not found")
            return
        }
        wk.modifyBy = user.id
        val workspace0 = WorkSpaceData().updateWorkSpace(wk)
        ResponseHelper.sendResponse(routeContext, HttpResponseStatus.OK, mapOf("code" to 0, "data" to workspace0))
    }

    @WebEndPoint(
        methods = [HttpMethod.PUT],
        path = "/api/workspace/:id/member/:type/add",
        authTypes = [AuthType.UserToken]
    )
    fun addWorkspaceMember(routeContext: RoutingContext) {
        val id = routeContext.request().getParam("id").toIntOrNull()
        val type = routeContext.request().getParam("type").toIntOrNull()
        if ((id == null) || (type == null)) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.BAD_REQUEST, "invalid parameter")
            return
        }
        val user = Authenticator.getUser(routeContext)
        val wk = WorkspaceCache.getWorkspace(id)
        if ((wk == null) || ((wk.ownerId != user.id)) && (WorkspaceCache.getWorkspaceUserInRoles(
                id,
                listOf(WorkSpaceUserRole.Admin)
            ).isEmpty())
        ) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.FORBIDDEN, "denied")
            return
        }
        val token = object : TypeToken<List<Long>>() {}
        val idList = Gson().fromJson<List<Long>>(routeContext.bodyAsString, token.type)
        WorkSpaceData().addMemberForWorkspace(id, WorkSpaceUserRole.parseInt(type), idList)
        ResponseHelper.sendResponse(routeContext, HttpResponseStatus.OK)
    }
}
