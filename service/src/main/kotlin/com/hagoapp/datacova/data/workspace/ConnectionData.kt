/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.entity.connection.ConnectionExtra
import com.hagoapp.datacova.entity.connection.WorkspaceConnection
import com.hagoapp.datacova.util.data.DatabaseFunctions
import java.sql.ResultSet
import java.sql.Timestamp

class ConnectionData(config: DatabaseConfig) : CoVaDatabase(config) {

    constructor() : this(CoVaConfig.getConfig().database)

    fun getWorkspaceConnections(workspaceId: Int): List<WorkspaceConnection> {
        connection.prepareStatement("select * from connection where wkid = ?").use { stmt ->
            stmt.setInt(1, workspaceId)
            stmt.executeQuery().use { rs ->
                val ret = mutableListOf<WorkspaceConnection>()
                while (rs.next()) {
                    ret.add(db2connection(rs))
                }
                return ret
            }
        }
    }

    private fun db2connection(rs: ResultSet): WorkspaceConnection {
        val con = WorkspaceConnection()
        with (con) {
            id = rs.getInt("id")
            name = rs.getString("name")
            description = rs.getString("description")
            workspaceId = rs.getInt("wkid")
            extra = ConnectionExtra.fromJson(rs.getString("extra"))
            addBy = rs.getLong("addby")
            addTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            modifyBy = DatabaseFunctions.getDBValue(rs, "modifyby")
            modifyTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "modifytime")?.toInstant()?.toEpochMilli()
            configuration = null
        }
        return con;
    }
}
