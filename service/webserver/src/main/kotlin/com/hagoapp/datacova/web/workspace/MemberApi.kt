/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.workspace

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.user.UserData
import com.hagoapp.datacova.data.workspace.WorkSpaceData
import com.hagoapp.datacova.data.workspace.WorkspaceCache
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.UserSearchResultItem
import com.hagoapp.datacova.user.UserType
import com.hagoapp.datacova.util.WorkspaceUserRoleUtil
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext

class MemberApi {
    @WebEndPoint(
        methods = [MethodName.PUT],
        path = "/api/workspace/:id/member/:type/add",
        authTypes = [AuthType.USER_TOKEN]
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
        if ((wk == null) || !WorkspaceUserRoleUtil.isAdmin(user, wk)) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.FORBIDDEN, "denied")
            return
        }
        val token = object : TypeToken<List<UserSearchResultItem>>() {}
        val users = Gson().fromJson<List<UserSearchResultItem>>(routeContext.body().asString(), token.type)
        val idList = users.map {
            if (it.id != null) it.id else {
                val u = UserInfo()
                with(u) {
                    userId = it.userId
                    name = it.name
                    userType = UserType.parseInt(it.provider)
                    mobile = it.mobile
                    email = it.email
                    pwdHash = ""
                }
                UserData(CoVaConfig.getConfig().database).addUserFromProvider(u).id
            }
        }
        WorkSpaceData(CoVaConfig.getConfig().database).addMemberForWorkspace(id, WorkSpaceUserRole.parseInt(type), idList)
        WorkspaceCache.clearWorkspaceUser(id)
        ResponseHelper.sendResponse(routeContext, HttpResponseStatus.OK, mapOf("code" to 0))
    }

    @WebEndPoint(
        methods = [MethodName.DELETE],
        path = "/api/workspace/:id/member/:type/remove/:uid",
        authTypes = [AuthType.USER_TOKEN]
    )
    fun removeWorkspaceMember(routeContext: RoutingContext) {
        val id = routeContext.request().getParam("id").toIntOrNull()
        val type = routeContext.request().getParam("type").toIntOrNull()
        val uid = routeContext.request().getParam("uid").toLongOrNull()
        if ((id == null) || (type == null) || (uid == null)) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.BAD_REQUEST, "invalid parameter")
            return
        }
        val user = Authenticator.getUser(routeContext)
        val wk = WorkspaceCache.getWorkspace(id)
        if ((wk == null) || !WorkspaceUserRoleUtil.isAdmin(user, wk)) {
            ResponseHelper.respondError(routeContext, HttpResponseStatus.FORBIDDEN, "denied")
            return
        }
        WorkSpaceData(CoVaConfig.getConfig().database).removeMemberForWorkspace(id, WorkSpaceUserRole.parseInt(type), listOf(uid))
        WorkspaceCache.clearWorkspaceUser(id)
        ResponseHelper.sendResponse(routeContext, HttpResponseStatus.OK, mapOf("code" to 0))
    }
}