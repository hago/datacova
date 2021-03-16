/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.entity.connection.ConnectionConfig

class ConnectionCache {
    companion object {
        private const val CONNECTION_LIST = "CONNECTION_LIST"

        @JvmStatic
        fun getConnections(workspaceId: Int): List<ConnectionConfig> {
            val token = object : TypeToken<List<ConnectionConfig>>() {}
            val l = RedisCacheReader.readCachedData(
                CONNECTION_LIST, 3600,
                object : RedisCacheReader.GenericLoader<List<ConnectionConfig>> {
                    override fun perform(vararg params: Any?): List<ConnectionConfig>? {
                        return ConnectionData().getWorkspaceConnections(params[0] as Int)
                    }
                }, token.type, workspaceId
            )
            return l ?: listOf()
        }
    }
}
