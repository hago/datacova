/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.user

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.data.user.UserCache
import com.hagoapp.datacova.user.UserFinder
import com.hagoapp.datacova.user.UserSearchReq
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext

class UserApi {

    @WebEndPoint(
        path = "/api/user/search",
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun searchUser(context: RoutingContext) {
        val load = context.body().asString()
        val search = Gson().fromJson(load, UserSearchReq::class.java)
        val users = UserFinder.search(search)
        ResponseHelper.sendResponse(
            context,
            HttpResponseStatus.OK,
            mapOf("code" to 0, "data" to users)
        )
    }

    @WebEndPoint(
        path = "/api/user/batch",
        methods = [MethodName.POST],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun batchGetUserInfo(context: RoutingContext) {
        val token = object : TypeToken<List<Long>>() {}
        val userIdList = Gson().fromJson<List<Long>>(context.body().asString(), token.type)
        val userInfoList = UserCache.batchGetUser(userIdList).map { it?.maskUserInfo() }
        ResponseHelper.sendResponse(
            context,
            HttpResponseStatus.OK,
            mapOf("code" to 0, "data" to userInfoList)
        )
    }

    @WebEndPoint(
        path = "/api/user/(\\d+)",
        isPathRegex = true,
        methods = [MethodName.GET],
        authTypes = [AuthType.USER_TOKEN]
    )
    fun getUser(context: RoutingContext) {
        val id = context.pathParam("param0").toLong()
        val user = UserCache.getUser(id)
        if (user == null) {
            ResponseHelper.respondError(context, HttpResponseStatus.NOT_FOUND, "user not found")
        } else {
            ResponseHelper.sendResponse(
                context,
                HttpResponseStatus.OK,
                mapOf("code" to 0, "data" to user)
            )
        }
    }
}
