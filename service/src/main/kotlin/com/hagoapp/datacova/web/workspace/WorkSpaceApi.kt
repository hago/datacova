package com.hagoapp.datacova.web.workspace

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.data.workspace.WorkSpaceData
import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.entity.workspace.WorkSpaceUsers
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class WorkSpaceApi {

    companion object {
        private const val MY_WORKSPACES_CACHE_KEY = "workspace_%d"
    }

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

    private fun getMyWorkSpaces(userId: Long): List<WorkSpace> {
        val key = MY_WORKSPACES_CACHE_KEY.format(userId)
        val token = object : TypeToken<List<WorkSpace>>() {}
        val workspaces = RedisCacheReader.readCachedData(
            key, 1800,
            object : RedisCacheReader.GenericLoader<List<WorkSpace>> {
                override fun perform(vararg params: Any?): List<WorkSpace> {
                    return if (params.isEmpty()) listOf() else
                        WorkSpaceData(CoVaConfig.getConfig().database).getMyWorkSpaces(params[0] as Long)
                }
            }, token.type, userId
        )
        return workspaces ?: listOf()
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
}
