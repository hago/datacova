/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.entity.connection.WorkspaceConnection
import com.hagoapp.datacova.utility.redis.RedisCacheReader

class ConnectionCache {
    companion object {
        private const val CONNECTION_LIST = "CONNECTION_LIST"

        @JvmStatic
        fun getConnections(workspaceId: Int): List<WorkspaceConnection> {
            val token = object : TypeToken<List<WorkspaceConnection>>() {}
            val l = RedisCacheReader.readCachedData(
                CONNECTION_LIST, 3600,
                { params -> ConnectionData(CoVaConfig.getConfig().database).getWorkspaceConnections(params[0] as Int) }, token.type, workspaceId
            )
            return l ?: listOf()
        }

        @JvmStatic
        fun clearConnections(workspaceId: Int) {
            RedisCacheReader.clearData(CONNECTION_LIST, workspaceId)
        }

        @JvmStatic
        fun getConnection(workspaceId: Int, id: Int): WorkspaceConnection? {
            return getConnections(workspaceId).firstOrNull { it.id == id }
        }
    }
}
