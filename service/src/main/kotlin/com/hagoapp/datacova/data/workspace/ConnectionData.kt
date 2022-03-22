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
import com.hagoapp.datacova.entity.connection.WorkspaceConnection
import com.hagoapp.datacova.util.data.DatabaseFunctions
import com.hagoapp.f2t.database.config.DbConfigReader
import java.sql.ResultSet
import java.sql.Timestamp

class ConnectionData(config: DatabaseConfig) : CoVaDatabase(config) {

    constructor() : this(CoVaConfig.getConfig().database)

    fun getWorkspaceConnections(workspaceId: Int): List<WorkspaceConnection> {
        val sql = "select * from connection where wkid = ? order by modifytime desc, addtime desc"
        connection.prepareStatement(sql).use { stmt ->
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
        with(con) {
            id = rs.getInt("id")
            name = rs.getString("name")
            description = rs.getString("description")
            workspaceId = rs.getInt("wkid")
            addBy = rs.getLong("addby")
            addTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            modifyBy = DatabaseFunctions.getDBValue(rs, "modifyby")
            modifyTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "modifytime")?.toInstant()?.toEpochMilli()
            configuration = DbConfigReader.json2DbConfig(rs.getString("configuration"))
        }
        return con;
    }

    fun getWorkspaceConnection(id: Int): WorkspaceConnection? {
        return connection.prepareStatement("select * from connection where id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                if (rs.next()) db2connection(rs) else null
            }
        }
    }

    fun addWorkspaceConnection(wkConnection: WorkspaceConnection): WorkspaceConnection {
        val sql = "insert into connection(name, description, configuration, wkid, addby, modifyby, modifytime)" +
                "values(?, ?, ?, ?, ?, ?, now()) returning id"
        val id = connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, wkConnection.name)
            stmt.setString(2, wkConnection.description)
            stmt.setObject(
                3,
                DatabaseFunctions.createPgObject("json", wkConnection.configuration.toJson())
            )
            stmt.setInt(4, wkConnection.workspaceId)
            stmt.setLong(5, wkConnection.addBy)
            stmt.setLong(6, wkConnection.addBy)
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.getInt(1)
            }
        }
        return getWorkspaceConnection(id)!!
    }

    fun deleteConnection(id: Int) {
        connection.prepareStatement("delete from connection where id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.execute()
        }
    }

    fun updateWorkspaceConnection(wkConnection: WorkspaceConnection): WorkspaceConnection {
        val sql = "update connection set name = ?, description = ?, configuration = ?, wkid = ?, " +
                "modifyby = ?, modifytime=now() where id = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, wkConnection.name)
            stmt.setString(2, wkConnection.description)
            stmt.setObject(
                3,
                DatabaseFunctions.createPgObject("json", wkConnection.configuration.toJson())
            )
            stmt.setInt(4, wkConnection.workspaceId)
            stmt.setLong(5, wkConnection.modifyBy)
            stmt.setInt(6, wkConnection.id)
            stmt.execute()
        }
        return getWorkspaceConnection(wkConnection.id)!!
    }
}
