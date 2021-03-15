package com.hagoapp.datacova.data.workspace

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
import com.hagoapp.datacova.util.data.DatabaseFunctions
import java.sql.ResultSet
import java.sql.Timestamp

class WorkSpaceData(connectionConfig: DatabaseConfig) : CoVaDatabase(connectionConfig) {

    constructor() : this(CoVaConfig.getConfig().database)

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
        val sql = "select distinct ws.* from workspace as ws inner join workspaceuser as wsu on ws.id = wsu.wkid " +
                "where wsu.userid = ? and ws.ownerid <> ?"
        val workspaces = mutableListOf<WorkSpace>()
        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, userId)
            stmt.setLong(2, userId)
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
            addBy = rs.getLong("addby")
            addTime = rs.getTimestamp("addtime").toInstant().toEpochMilli()
            modifyBy = DatabaseFunctions.getDBValue(rs, "modifyby")
            modifyTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "modifytime")?.toInstant()?.toEpochMilli()
            ownerId = rs.getLong("ownerid")
        }
        return workSpace
    }

    fun addWorkSpace(workSpace: WorkSpace): WorkSpace? {
        connection.autoCommit = false
        connection.prepareStatement("select id from workspace where name = ? and ownerid = ?").use { stmt ->
            stmt.setString(1, workSpace.name)
            stmt.setLong(2, workSpace.ownerId)
            stmt.executeQuery().use { rs ->
                if (rs.next()) {
                    connection.rollback()
                    return null
                }
            }
        }
        val id = try {
            connection.prepareStatement("insert into workspace (name, description, ownerid, addby) values (?,?,?,?) returning id")
                .use { stmt ->
                    stmt.setString(1, workSpace.name)
                    stmt.setString(2, workSpace.description)
                    stmt.setLong(3, workSpace.ownerId)
                    stmt.setLong(4, workSpace.addBy)
                    stmt.executeQuery().use { rs ->
                        rs.next()
                        rs.getInt(1)
                    }
                }
        } catch (ex: Exception) {
            logger.error("create workspace in database failed: {}", ex.message)
            connection.rollback()
            return null
        }
        connection.prepareStatement("insert into workspaceuser (wkid, usergroup, userid) values (?,?,?)").use { stmt ->
            stmt.setInt(1, id)
            stmt.setInt(2, WorkSpaceUserRole.Admin.value)
            stmt.setLong(3, workSpace.ownerId)
            stmt.addBatch()
            stmt.executeBatch()
            stmt.clearParameters()
        }
        connection.commit()
        connection.autoCommit = true
        return getWorkSpace(id)
    }

    /**
     * get a single workspace data
     */
    fun getWorkSpace(id: Int): WorkSpace? {
        return connection.prepareStatement("select * from workspace where id = ?").use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                if (!rs.next()) null
                else resultSet2WorkSpace(rs)
            }
        }
    }

    /**
     * get user list with role of a
     */
    fun getWorkspaceUserIdList(id: Int): List<WorkspaceBasicUser> {
        val sql = "select u.id as userid, wsu.usergroup " +
                "from workspaceuser as wsu inner join users as u on wsu.userid = u.id " +
                "where wkid = ?"
        return connection.prepareCall(sql).use { stmt ->
            stmt.setInt(1, id)
            stmt.executeQuery().use { rs ->
                val list = mutableListOf<WorkspaceBasicUser>()
                while (rs.next()) {
                    list.add(
                        WorkspaceBasicUser(
                            id,
                            rs.getLong("userid"),
                            WorkSpaceUserRole.parseInt(rs.getInt("usergroup"))
                        )
                    )
                }
                list
            }
        }
    }

    data class WorkspaceBasicUser(
        val workspaceId: Int,
        val userid: Long,
        val role: WorkSpaceUserRole
    )
}
