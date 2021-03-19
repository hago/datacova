/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.workspace

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.data.IDatabaseConnection
import com.hagoapp.datacova.data.workspace.ConnectionCache
import com.hagoapp.datacova.data.workspace.ConnectionData
import com.hagoapp.datacova.data.workspace.WorkspaceCache
import com.hagoapp.datacova.entity.connection.ConnectionConfigFactory
import com.hagoapp.datacova.entity.connection.WorkspaceConnection
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class Connection {

    private val logger = CoVaLogger.getLogger()

    @WebEndPoint(
        path = "/api/workspace/:id/connections",
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
        methods = [HttpMethod.POST],
        authTypes = [AuthType.UserToken]
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

    @WebEndPoint(
        path = "/api/workspace/:id/connection/add",
        methods = [HttpMethod.PUT],
        authTypes = [AuthType.UserToken]
    )
    fun addConnection(context: RoutingContext) {
        val id = context.pathParam("id").toInt()
        if (WorkspaceCache.getWorkspace(id) == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "Invalid workspace")
            return
        }
        val json = context.bodyAsString
        val wsCon = WorkspaceConnection.load(json)
        if (wsCon == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "Invalid Connection")
            return
        }
        val user = Authenticator.getUser(context)
        if (WorkspaceCache.getWorkspaceUserInRoles(
                id,
                listOf(WorkSpaceUserRole.Admin, WorkSpaceUserRole.Maintainer)
            ).none { it.userid == user.id }
        ) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "Access Denied")
            return
        }
        wsCon.workspaceId = id
        wsCon.addBy = Authenticator.getUser(context).id
        val wk = ConnectionData().addWorkspaceConnection(wsCon)
        ConnectionCache.clearConnections(id)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to wk
            )
        )
    }

    @WebEndPoint(
        path = "/api/workspace/:id/connection/update",
        methods = [HttpMethod.PUT],
        authTypes = [AuthType.UserToken]
    )
    fun updateConnection(context: RoutingContext) {
        val id = context.pathParam("id").toInt()
        val workspace = WorkspaceCache.getWorkspace(id)
        if (workspace == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "Invalid workspace")
            return
        }
        val json = context.bodyAsString
        val wsCon = WorkspaceConnection.load(json)
        if (wsCon == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "Invalid Connection")
            return
        }
        if (ConnectionCache.getConnection(id, wsCon.id) == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "Invalid Connection")
            return
        }
        val user = Authenticator.getUser(context)
        if ((workspace.ownerId != user.id) && WorkspaceCache.getWorkspaceUserInRoles(
                id,
                listOf(WorkSpaceUserRole.Admin, WorkSpaceUserRole.Maintainer)
            ).none { it.userid == user.id }
        ) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "Access Denied")
            return
        }
        wsCon.workspaceId = id
        wsCon.modifyBy = Authenticator.getUser(context).id
        val wk = ConnectionData().updateWorkspaceConnection(wsCon)
        ConnectionCache.clearConnections(id)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to wk
            )
        )
    }

    @WebEndPoint(
        path = "/api/workspace/:wkid/connection/:id/delete",
        methods = [HttpMethod.DELETE],
        authTypes = [AuthType.UserToken]
    )
    fun deleteConnection(context: RoutingContext) {
        val workspaceId = context.pathParam("wkid").toInt()
        val connectionId = context.pathParam("id").toInt()
        val connection = ConnectionCache.getConnection(workspaceId, connectionId)
        if ((connection == null) || (connection.workspaceId != workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "Invalid request")
            return
        }
        val user = Authenticator.getUser(context)
        if (WorkspaceCache.getWorkspaceUserInRoles(
                workspaceId,
                listOf(WorkSpaceUserRole.Admin, WorkSpaceUserRole.Maintainer)
            ).none { it.userid == user.id }
        ) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "Access Denied")
            return
        }
        ConnectionData().deleteConnection(connectionId)
        ConnectionCache.clearConnections(workspaceId)
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0))
    }

    @WebEndPoint(
        path = "/api/workspace/:wkid/connection/:id",
        methods = [HttpMethod.GET],
        authTypes = [AuthType.UserToken]
    )
    fun getConnection(context: RoutingContext) {
        val workspaceId = context.pathParam("wkid").toInt()
        val connectionId = context.pathParam("id").toInt()
        val connection = ConnectionCache.getConnection(workspaceId, connectionId)
        if ((connection == null) || (connection.workspaceId != workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "Invalid request")
            return
        }
        val user = Authenticator.getUser(context)
        val roles = WorkspaceCache.getWorkspaceUserInRoles(workspaceId)
        if (roles.none { it.userid == user.id }) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "Access Denied")
            return
        }
        val writable = (WorkspaceCache.getWorkspace(workspaceId)?.ownerId == user.id) ||
                roles.any {
                    it.userid == user.id &&
                            ((it.role == WorkSpaceUserRole.Admin) || (it.role == WorkSpaceUserRole.Maintainer))
                }
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to mapOf(
                    "connection" to connection,
                    "permission" to mapOf(
                        "update" to writable,
                        "delete" to writable
                    )
                )
            )
        )
    }

    @WebEndPoint(
        path = "/api/workspace/:wkid/connection/:id/tables",
        methods = [HttpMethod.GET],
        authTypes = [AuthType.UserToken]
    )
    fun listConnectionTables(context: RoutingContext) {
        val connection = ConnectionCache.getConnection(
            context.pathParam("wkid").toInt(),
            context.pathParam("id").toInt()
        )
        if (connection == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "invalid connection")
            return
        }
        val user = Authenticator.getUser(context)
        if (WorkspaceCache.getWorkspaceUserInRoles(connection.workspaceId).none { it.userid == user.id }) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "connection access denied")
            return
        }
        val db = IDatabaseConnection.getDatabaseConnection(connection.configuration)
        ResponseHelper.sendResponse(
            context,
            HttpResponseStatus.OK,
            mapOf("code" to 0, "data" to db.getAvailableTables(connection.configuration))
        )
    }
}
