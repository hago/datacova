/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.user.LocalUserProvider
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.UserStatus
import com.hagoapp.datacova.user.UserType
import com.hagoapp.datacova.util.Utils
import com.hagoapp.datacova.util.data.DatabaseFunctions
import java.sql.ResultSet
import java.sql.Timestamp

class UserData(config: DatabaseConfig) : CoVaDatabase(config) {

    companion object {
        fun computePwdHash(password: String): String {
            return Utils.sha1Digest("$password|${Utils.sha256Digest(password)}")
        }
    }

    constructor() : this(CoVaConfig.getConfig().database)

    fun findUser(userId: String): UserInfo? {
        val sql = "select * from users where userid = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, userId)
            stmt.executeQuery().use { rs ->
                return if (!rs.next()) null else row2User(rs)
            }
        }
    }

    fun findUser(userId: String, userType: Int): UserInfo? {
        val sql = "select * from users where userid = ? and usertype = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, userId)
            stmt.setInt(2, userType)
            stmt.executeQuery().use { rs ->
                return if (!rs.next()) null else row2User(rs)
            }
        }
    }

    fun findUser(id: Long): UserInfo? {
        val sql = "select * from users where id = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, id)
            stmt.executeQuery().use { rs ->
                return if (!rs.next()) null else row2User(rs)
            }
        }
    }

    private fun row2User(rs: ResultSet): UserInfo {
        val user = UserInfo(rs.getString("userid"), LocalUserProvider.PROVIDER_NAME)
        with(user) {
            id = rs.getLong("id")
            name = rs.getString("name")
            description = rs.getString("description")
            addBy = rs.getLong("addby")
            addTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "addtime")!!.toInstant().toEpochMilli()
            modifyBy = DatabaseFunctions.getDBValue(rs, "modifyby")
            modifyTime = DatabaseFunctions.getDBValue<Timestamp>(rs, "modifytime")?.toInstant()?.toEpochMilli()
            thumbnail = DatabaseFunctions.getDBValue<ByteArray>(rs, "thumbnail")
            status = UserStatus.parseInt(rs.getInt("eustatus"))
            pwdHash = rs.getString("pwdhash")
            userType = UserType.parseInt(rs.getInt("usertype"))
            provider = rs.getString("usertype")
        }
        return user
    }
}
