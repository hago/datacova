package com.hagoapp.datacova.data.workspace

import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.util.data.DatabaseFunctions
import java.sql.ResultSet
import java.sql.Timestamp

class WorkSpaceData(connectionConfig: DatabaseConfig) : CoVaDatabase(connectionConfig) {
    /**
     * get all work spaces owned by user
     */
    fun getOwnedWorkSpaces(userId: Long): List<WorkSpace> {
        val sql = "select * from workspace where ownerid = ?"
        val workspaces = mutableListOf<WorkSpace>()
        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, userId)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    workspaces.add(resultSet2WorkSpace(rs))
                }
            }
        }
        return workspaces
    }

    /**
     * get all work spaces the user is involved
     */
    private fun getInvolvedWorkSpaces(userId: Long): List<WorkSpace> {
        val sql =
            "select distinct ws.* from workspace as ws inner join workspaceuser as wsu on ws.id = wsu.wkid where wsu.userid = ?"
        val workspaces = mutableListOf<WorkSpace>()
        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, userId)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    workspaces.add(resultSet2WorkSpace(rs))
                }
            }
        }
        return workspaces
    }

    /**
     * get all work spaces a user has something to do with
     */
    fun getMyWorkSpaces(userId: Long): List<WorkSpace> {
        val owned = getOwnedWorkSpaces(userId)
        return getInvolvedWorkSpaces(userId).plus(owned)
    }

    /**
     * convert data from table to work space object
     */
    private fun resultSet2WorkSpace(rs: ResultSet): WorkSpace {
        val workSpace = WorkSpace()
        with(workSpace) {
            id = rs.getInt("id")
            name = rs.getString("name")
            description = rs.getString("description")
            addBy = rs.getString("addby")
            addTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            modifyBy = DatabaseFunctions.getDBValue<String>(rs, "modifyby")
            modifyTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "modifytime")?.toInstant()?.toEpochMilli()
            ownerId = rs.getString("ownerid")
        }
        return workSpace
    }
}
