/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.user

import com.hagoapp.datacova.utility.Utils
import com.hagoapp.datacova.lib.data.CoVaDatabase
import com.hagoapp.datacova.lib.data.DatabaseConfig
import com.hagoapp.datacova.lib.data.DatabaseFunctions
import com.hagoapp.datacova.user.*
import java.sql.ResultSet
import java.sql.Timestamp

class UserData(config: DatabaseConfig) : CoVaDatabase(config) {

    companion object {
        @JvmStatic
        fun computePwdHash(password: String): String {
            return Utils.sha256Digest("$password|${Utils.sha256Digest(password)}")
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
            thumbnail = DatabaseFunctions.getDBValue(rs, "thumbnail")
            status = UserStatus.parseInt(rs.getInt("eustatus"))
            pwdHash = rs.getString("pwdhash")
            userType = UserType.parseInt(rs.getInt("usertype"))
            provider = rs.getString("usertype")
            email = rs.getString("email")
            mobile = rs.getString("mobile")
        }
        if (user.thumbnail != null) {
            UserAuthFactory.getAuthProvider(user.userType.value).loadThumbnail(user)
        }
        return user
    }

    private data class BasicUser(
        val name: String,
        val description: String
    )

    fun searchUser(word: String, providers: List<Int> = listOf(), limit: Int = 10): List<UserInfo> {
        var sql = "select * from users where "
        if (providers.isNotEmpty()) {
            sql += "usertype in (${providers.joinToString(", ") { "?" }}) and "
        }
        sql += " userid ilike ? or name ilike ? or description ilike ? limit ?"
        connection.prepareStatement(sql).use { stmt ->
            providers.forEachIndexed { index, i -> stmt.setInt(index + 1, i) }
            val keyword = "%${DatabaseFunctions.escapePGLike(word)}%"
            stmt.setString(1 + providers.size, keyword)
            stmt.setString(2 + providers.size, keyword)
            stmt.setString(3 + providers.size, keyword)
            stmt.setInt(4 + providers.size, limit)
            val users = mutableListOf<UserInfo>()
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    users.add(row2User(rs))
                }
            }
            return users
        }
    }

    fun batchGetUser(idList: Set<Long>): List<UserInfo?> {
        if (idList.isEmpty()) {
            return listOf()
        }
        val ret = idList.map { null }.toMutableList<UserInfo?>()
        val sql = "select * from users where id in (${idList.joinToString(", ") { "?" }})"
        connection.prepareStatement(sql).use { stmt ->
            idList.forEachIndexed { index, id -> stmt.setLong(index + 1, id) }
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    val userInfo = row2User(rs)
                    val index = idList.indexOf(userInfo.id)
                    ret[index] = userInfo
                }
            }
        }
        return ret
    }

    fun registerUser(user: UserInfo): UserInfo {
        val sql = """insert into users (userid, pwdhash, email, mobile, addby, modifyby, modifytime, name, usertype, 
            thumbnail, eustatus) values (?, ?, ?, ?, -1, -1, now(), ?, 0, ?, -2) returning id"""
        val id = connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, user.userId)
            stmt.setString(2, user.pwdHash)
            stmt.setString(3, user.email)
            stmt.setString(4, user.mobile)
            stmt.setString(5, user.name)
            stmt.setString(6, user.thumbnail)
            stmt.executeQuery().use { rs ->
                rs.next()
                rs.getLong("id")
            }
        }
        return findUser(id)!!
    }

    fun isUserIdExisted(userId: String): Boolean {
        val sql = "select id from users where usertype = 0 and userId = ?"
        return connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, userId)
            stmt.executeQuery().use { rs ->
                rs.next()
            }
        }
    }

    fun isEmailExisted(email: String): Boolean {
        val sql = "select id from users where usertype = 0 and email = ?"
        return connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, email)
            stmt.executeQuery().use { rs ->
                rs.next()
            }
        }
    }

    fun isMobileExisted(mobile: String): Boolean {
        val sql = "select id from users where usertype = 0 and mobile = ?"
        return connection.prepareStatement(sql).use { stmt ->
            stmt.setString(1, mobile)
            stmt.executeQuery().use { rs ->
                rs.next()
            }
        }
    }

    fun activateUser(id: Long): Int {
        connection.prepareStatement("update users set eustatus = 0 where id = ?").use { stmt ->
            stmt.setLong(1, id)
            return stmt.executeUpdate()
        }
    }

    fun addUserFromProvider(user: UserInfo): UserInfo {
        connection.autoCommit = false
        val select = "select id from users where userid = ? and usertype = ?"
        var id = connection.prepareStatement(select).use { stmt ->
            stmt.setString(1, user.userId)
            stmt.setInt(2, user.userType.value)
            stmt.executeQuery().use { rs ->
                if (rs.next()) rs.getLong("id") else null
            }
        }
        if (id == null) {
            val sql = """insert into users 
            (userid, email, mobile, addby, modifyby, modifytime, name, usertype, eustatus, pwdhash) 
            values (?, ?, ?, -1, -1, now(), ?, ?, 0, ?) returning id"""
            id = connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, user.userId)
                stmt.setString(2, user.email)
                stmt.setString(3, user.mobile)
                stmt.setString(4, user.name)
                stmt.setInt(5, user.userType.value)
                stmt.setString(6, user.pwdHash)
                stmt.executeQuery().use { rs ->
                    rs.next()
                    rs.getLong("id")
                }
            }
        } else {
            val sql = """update users set name = ?, email = ?, mobile = ?, modifytime = now() where id = ?"""
            connection.prepareStatement(sql).use { stmt ->
                stmt.setString(1, user.name)
                stmt.setString(2, user.email)
                stmt.setString(3, user.mobile)
                stmt.setLong(4, id)
                stmt.execute()
            }
        }
        connection.commit()
        return findUser(id)!!
    }
}
