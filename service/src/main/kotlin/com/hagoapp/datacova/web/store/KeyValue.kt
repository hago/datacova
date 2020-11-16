/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.store

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.util.http.RequestHelper
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class KeyValue {

    @WebEndPoint(methods = [HttpMethod.POST, HttpMethod.PUT], path = "/store/pair", authTypes = [AuthType.UserToken])
    fun storeKeyValue(context: RoutingContext) {
        val kv = RequestHelper.readBodyClass(context, KeyValueRequest::class.java)
        if (kv != null) {
            saveKeyPairs(Authenticator.getUser(context), listOf(kv))
            ResponseHelper.sendResponse(
                context, HttpResponseStatus.OK, mapOf(
                    "code" to 0,
                    "data" to kv
                )
            )
        } else {
            context.fail(HttpResponseStatus.BAD_REQUEST.code(), CoVaException("invalid body"))
        }
    }

    @WebEndPoint(methods = [HttpMethod.POST, HttpMethod.PUT], path = "/store/pairs", authTypes = [AuthType.UserToken])
    fun storeKeyValues(context: RoutingContext) {
        val token = object : TypeToken<List<KeyValueRequest>>() {}
        val json = RequestHelper.readBodyString(context)
        try {
            val lst = Gson().fromJson<List<KeyValueRequest>>(json, token.type)
            if (lst != null) {
                saveKeyPairs(Authenticator.getUser(context), lst)
                ResponseHelper.sendResponse(
                    context, HttpResponseStatus.OK, mapOf(
                        "code" to 0,
                        "data" to lst
                    )
                )
            } else {
                context.fail(HttpResponseStatus.BAD_REQUEST.code(), CoVaException("empty body"))
            }
        } catch (e: JsonSyntaxException) {
            context.fail(HttpResponseStatus.BAD_REQUEST.code(), CoVaException("invalid body"))
        }
    }

    private fun saveKeyPairs(userInfo: UserInfo, pairs: List<KeyValueRequest>) {
        //TODO("store key values")
    }
}