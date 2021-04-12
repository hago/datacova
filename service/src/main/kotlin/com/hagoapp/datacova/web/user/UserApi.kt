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
import com.hagoapp.datacova.data.user.UserData
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class UserApi {

    @WebEndPoint(
        path = "/api/user/search",
        methods = [HttpMethod.POST],
        authTypes = [AuthType.UserToken]
    )
    fun searchUser(context: RoutingContext) {
        val word = context.bodyAsString
        val users = UserData().searchUser(word)
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0, "data" to users))
    }

    @WebEndPoint(
        path = "/api/user/batch",
        methods = [HttpMethod.POST],
        authTypes = [AuthType.UserToken]
    )
    fun batchGetUserInfo(context: RoutingContext) {
        val token = object : TypeToken<List<Long>>() {}
        val userIdList = Gson().fromJson<List<Long>>(context.bodyAsString, token.type)
        val userInfoList = UserCache.batchGetUser(userIdList)
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf("code" to 0, "data" to userInfoList))
    }
}
