/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data

import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.user.LocalUserProvider
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.UserStatus
import com.hagoapp.datacova.user.UserType
import com.hagoapp.datacova.util.Utils
import com.hagoapp.datacova.util.data.getDBValue
import java.sql.ResultSet
import java.sql.Timestamp

class UserData(config: DatabaseConfig) : CoVaDatabase(config) {

    companion object {
        fun computePwdHash(password: String): String {
            return Utils.sha1Digest("$password|${Utils.sha256Digest(password)}")
        }
    }

    fun findUser(userId: String): UserInfo? {
        val sql = "select * from users where userid = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, userId)
            stmt.executeQuery().use { rs ->
                return if (!rs.next()) null else row2User(rs)
            }
        }
    }

    private fun row2User(rs: ResultSet): UserInfo {
        val user = UserInfo(rs.getString("userid"), LocalUserProvider.PROVIDER_NAME)
        with(user) {
            name = rs.getString("name")
            description = rs.getString("description")
            addBy = rs.getString("addby")
            addTime = getDBValue<Timestamp>(rs, "addtime")!!.toInstant().toEpochMilli()
            modifyBy = getDBValue(rs, "modifyby")
            modifyTime = getDBValue<Timestamp>(rs, "modifytime")?.toInstant()?.toEpochMilli()
            thumbnail = getDBValue<ByteArray>(rs, "thumbnail")
            status = UserStatus.parseInt(rs.getInt("eustatus"))
            pwdHash = rs.getString("pwdhash")
            userType = UserType.parseInt(rs.getInt("usertype"))
        }
        return user
    }

    fun authenticate(userId: String, password: String): Boolean {
        val sql = "select * from users where userid = ? and pwdhash = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, userId)
            stmt.setString(2, computePwdHash(password))
            stmt.executeQuery().use { rs ->
                return rs.next()
            }
        }
    }
}
