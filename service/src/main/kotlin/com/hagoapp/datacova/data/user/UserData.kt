/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.user

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.user.LocalUserProvider
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.UserStatus
import com.hagoapp.datacova.user.UserType
import com.hagoapp.datacova.util.Utils
import com.hagoapp.datacova.util.data.DatabaseFunctions
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant

class UserData(config: DatabaseConfig) : CoVaDatabase(config) {

    companion object {
        private val basicUsers = mutableListOf<BasicUser>()
        private var userLoadedAt: Long = Instant.now().toEpochMilli()
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

    private data class BasicUser(
        val name: String,
        val description: String
    )

    private fun loadBasicUser() {

    }

    fun searchUser(word: String, limit: Int = 10): List<UserInfo> {
        val sql = "select * from users where userid ilike ? or name ilike ? or description ilike ? limit ?"
        connection.prepareStatement(sql).use { stmt ->
            val keyword = "%${DatabaseFunctions.escapePGLike(word)}%"
            stmt.setString(1, keyword)
            stmt.setString(2, keyword)
            stmt.setString(3, keyword)
            stmt.setInt(4, limit)
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
        println(sql)
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
}
