/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.workspace

import com.hagoapp.datacova.data.IDatabaseConnection
import com.hagoapp.datacova.data.workspace.ConnectionCache
import com.hagoapp.datacova.data.workspace.WorkspaceCache
import com.hagoapp.datacova.entity.connection.ConnectionConfigFactory
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class Connection {

    @WebEndPoint(
        path = "/api/workspace/:id/connection",
        methods = [HttpMethod.GET],
        authTypes = [AuthType.UserToken]
    )
    fun getConnections(context: RoutingContext) {
        val id = context.pathParam("id").toIntOrNull()
        if (id == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid workspace")
            return
        }
        val wk = WorkspaceCache.getWorkspace(id)
        if (wk == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid workspace")
            return
        }
        val user = Authenticator.getUser(context)
        val l = ConnectionCache.getConnections(id)
        val roles = WorkspaceCache.getWorkspaceUserInRoles(id).filter { it.userid == user.id }.map { it.role }
        val isWorkspaceOwner = wk.ownerId == user.id
        val data = mapOf(
            "connections" to l,
            "owner" to isWorkspaceOwner,
            "canDelete" to when {
                isWorkspaceOwner -> l.map { it.id }
                else -> l.filter { it.addBy == user.id }.map { it.id }
            },
            "canModify" to when {
                isWorkspaceOwner || roles.any {
                    (it == WorkSpaceUserRole.Admin) || (it == WorkSpaceUserRole.Maintainer)
                } -> l.map { it.id }
                else -> listOf()
            }
        )
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to data
            )
        )
    }

    @WebEndPoint(
        path = "/api/connection/verify",
        methods = [HttpMethod.POST]
    )
    fun verifyConnection(context: RoutingContext) {
        val json = context.bodyAsString
        val conf = ConnectionConfigFactory.getConnectionConfig(json)
        val con = IDatabaseConnection.getDatabaseConnection(conf)
        val result = con.canConnect(conf)
        if (result.first) {
            ResponseHelper.sendResponse(
                context, HttpResponseStatus.OK, mapOf(
                    "code" to 0,
                    "data" to mapOf(
                        "result" to result.first,
                        "message" to result.second,
                        "databases" to con.listDatabases(conf)
                    )
                )
            )
        } else {
            ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, "connection fail")
        }
    }
}
