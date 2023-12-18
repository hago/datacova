package com.hagoapp.datacova.web.workspace

import com.google.gson.GsonBuilder
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.execution.TaskExecutionCache
import com.hagoapp.datacova.data.execution.TaskExecutionCache.Companion.TASK_EXECUTION_DEFAULT_PAGE_SIZE
import com.hagoapp.datacova.data.rules.ValidationRuleCache
import com.hagoapp.datacova.lib.data.ValidationRuleData
import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.data.workspace.WorkSpaceData
import com.hagoapp.datacova.data.workspace.WorkspaceCache
import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
import com.hagoapp.datacova.lib.verification.Rule
import com.hagoapp.datacova.util.WorkspaceUserRoleUtil
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext

class WorkSpaceApi {

    @WebEndPoint(
        methods = [MethodName.GET],
        path = "/api/workspaces/mine",
        authTypes = [AuthType.USER_TOKEN]
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
            workspaceWithUser(it)
        } ?: listOf()
    }

    private fun workspaceWithUser(it: WorkSpace) = WorkspaceWithUser(
        workspace = it,
        users = findWorkspaceUsers(it.id),
        owner = UserCache.getUser(it.ownerId)!!
    )

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
        methods = [MethodName.GET],
        path = "/api/workspace/:id",
        authTypes = [AuthType.USER_TOKEN]
    )
    fun getWorkSpace(context: RoutingContext) {
        val userInfo = Authenticator.getUser(context)
        val workspaceId = context.pathParam("id").toInt()
        val wk = WorkspaceCache.getWorkspace(workspaceId)
        if ((wk == null) || !WorkspaceUserRoleUtil.isUser(userInfo, workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "Invalid Workspace Data")
            return
        }
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to workspaceWithUser(wk)
            )
        )
    }

    @WebEndPoint(
        methods = [MethodName.PUT, MethodName.POST],
        path = "/api/workspace/add",
        authTypes = [AuthType.USER_TOKEN]
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
        val workSpace = WorkSpaceData(CoVaConfig.getConfig().database).addWorkSpace(wk)
        WorkspaceCache.clearMyWorkspaces(userInfo.id)
        if (workSpace == null) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.CONFLICT, "duplicated name")
        } else {
            ResponseHelper.sendResponse(routeContext, HttpResponseStatus.OK, mapOf("code" to 0, "data" to workSpace))
        }
    }

    private fun parseWorkSpace(routeContext: RoutingContext): WorkSpace? {
        return try {
            val load = routeContext.body().asString()
            GsonBuilder().create().fromJson(load, WorkSpace::class.java)
        } catch (ex: Exception) {
            null
        }
    }

    @WebEndPoint(
        methods = [MethodName.PUT],
        path = "/api/workspace/update",
        authTypes = [AuthType.USER_TOKEN]
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
        val workspace0 = WorkSpaceData(CoVaConfig.getConfig().database).updateWorkSpace(wk)
        WorkspaceCache.clearMyWorkspaces(user.id)
        ResponseHelper.sendResponse(routeContext, HttpResponseStatus.OK, mapOf("code" to 0, "data" to workspace0))
    }

    @WebEndPoint(
        methods = [MethodName.GET],
        path = "/api/workspace/:id/executions",
        authTypes = [AuthType.USER_TOKEN]
    )
    fun getExecutionList(context: RoutingContext) {
        val id = context.pathParam("id").toInt()
        doGetExecutionRage(context, id, 0, TASK_EXECUTION_DEFAULT_PAGE_SIZE)
    }

    @WebEndPoint(
        methods = [MethodName.GET],
        path = "/api/workspace/:id/executions/:size",
        authTypes = [AuthType.USER_TOKEN]
    )
    fun getExecutionSizedList(context: RoutingContext) {
        val id = context.pathParam("id").toInt()
        val size = context.pathParam("size").toInt()
        doGetExecutionRage(context, id, 0, size)
    }

    @WebEndPoint(
        methods = [MethodName.GET],
        path = "/api/workspace/:id/executions/:start/:size",
        authTypes = [AuthType.USER_TOKEN]
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

    @WebEndPoint(
        path = "/api/execution/:id",
        methods = [MethodName.GET],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun getTaskExecution(context: RoutingContext) {
        val id = context.pathParam("id").toIntOrNull()
        if (id == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.NOT_FOUND, "Not existed")
            return
        }
        val execution = TaskExecutionCache.getTaskExecution(id)
        if (execution == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.NOT_FOUND, "Not existed")
            return
        } else {
            ResponseHelper.sendResponse(
                context, HttpResponseStatus.OK, mapOf(
                    "code" to 0,
                    "data" to execution
                )
            )
        }
    }

    @WebEndPoint(
        path = "/api/workspace/:wkid/rules/:start/:size",
        methods = [MethodName.GET],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun getRules(context: RoutingContext) {
        val workspaceId = context.pathParam("wkid").toInt()
        val start = context.pathParam("start").toInt()
        val size = context.pathParam("size").toInt()
        val user = Authenticator.getUser(context)
        val workspace = WorkspaceCache.getWorkspace(workspaceId)
        if ((workspace == null) || !WorkspaceUserRoleUtil.isUser(user, workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.UNAUTHORIZED, "access denied")
            return
        }
        val rules = ValidationRuleCache.getRules(workspaceId, start, size)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to rules
            )
        )
    }

    @WebEndPoint(
        path = "/api/workspace/:wkid/rule",
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun saveRule(context: RoutingContext) {
        val json = context.body().asString()
        val rule = Rule.fromJson(json)
        val user = Authenticator.getUser(context)
        val workspace = WorkspaceCache.getWorkspace(rule.workspaceId)
        if ((workspace == null) || !WorkspaceUserRoleUtil.isAnyRolesOf(
                user,
                workspace,
                setOf(WorkSpaceUserRole.ADMIN, WorkSpaceUserRole.MAINTAINER)
            )
        ) {
            ResponseHelper.respondError(context, HttpResponseStatus.UNAUTHORIZED, "access denied")
            return
        }
        if (rule.name == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid data")
            return
        }
        rule.modifyBy = user.id
        val rule1: Rule?
        if (rule.id <= 0) {
            rule.addBy = user.id
            rule1 = ValidationRuleData(CoVaConfig.getConfig().database).newRule(rule)
        } else {
            val rule0 = ValidationRuleCache.getRule(rule.id)
            if (rule.id != rule0?.id) {
                ResponseHelper.respondError(context, HttpResponseStatus.UNAUTHORIZED, "illegal update!")
                return
            }
            rule1 = ValidationRuleData(CoVaConfig.getConfig().database).updateRule(rule)
            ValidationRuleCache.clearRule(rule.id)
        }
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to rule1
            )
        )
    }
}
