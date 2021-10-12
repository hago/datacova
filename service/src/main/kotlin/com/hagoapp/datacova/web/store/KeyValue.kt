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
import com.hagoapp.datacova.config.init.CoVaConfig
import com.hagoapp.datacova.data.redis.JedisManager
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.util.KeyValuePair
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
        val kv = RequestHelper.readBodyClass(context, KeyValuePair::class.java)
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
        val token = object : TypeToken<List<KeyValuePair>>() {}
        val json = RequestHelper.readBodyString(context)
        try {
            val lst = Gson().fromJson<List<KeyValuePair>>(json, token.type)
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

    private fun saveKeyPairs(userInfo: UserInfo, pairs: List<KeyValuePair>) {
        JedisManager.getJedis(CoVaConfig.getConfig().redis).use {
            it.hset(userInfo.toString(), pairs.map { item -> Pair(item.key, item.value) }.toMap())
        }
    }

    @WebEndPoint(methods = [HttpMethod.GET], path = "/store/pair/:key", authTypes = [AuthType.UserToken])
    fun readKey(context: RoutingContext) {
        JedisManager.getJedis(CoVaConfig.getConfig().redis).use {
            val key = context.request().getParam("key")
            val value = it.hget(
                Authenticator.getUser(context).toString(),
                context.request().getParam("key")
            )
            ResponseHelper.sendResponse(
                context, HttpResponseStatus.OK, mapOf(
                    "code" to 0,
                    "data" to KeyValuePair(key, if (value == "nil") null else value)
                )
            )
        }
    }

    @WebEndPoint(methods = [HttpMethod.POST], path = "/store/pairs/fetch", authTypes = [AuthType.UserToken])
    fun readMultipleKeys(context: RoutingContext) {
        val json = RequestHelper.readBodyString(context)
        val token = object : TypeToken<List<KeyValuePair>>() {}
        try {
            val keys = Gson().fromJson<List<String>>(json, token.type)
            JedisManager.getJedis(CoVaConfig.getConfig().redis).use {
                val values = it.hmget(Authenticator.getUser(context).toString(), *keys.toTypedArray())
                val pairs = Array(keys.size) { i ->
                    KeyValuePair(
                        keys[i],
                        if (values[i] == "nil") null else values[i]
                    )
                }
                ResponseHelper.sendResponse(
                    context, HttpResponseStatus.OK, mapOf(
                        "code" to 0,
                        "data" to pairs
                    )
                )
            }
        } catch (e: JsonSyntaxException) {
            context.fail(HttpResponseStatus.BAD_REQUEST.code(), CoVaException("invalid body"))
        }
    }
}