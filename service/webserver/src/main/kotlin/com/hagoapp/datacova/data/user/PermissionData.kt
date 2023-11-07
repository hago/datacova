/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.user

import com.hagoapp.datacova.lib.data.CoVaDatabase
import com.hagoapp.datacova.lib.data.DatabaseConfig
import com.hagoapp.datacova.user.UserInfo
import com.hagoapp.datacova.user.permission.Permission
import com.hagoapp.datacova.user.permission.Role
import com.hagoapp.datacova.user.permission.UserPermissions
import com.hagoapp.datacova.user.permission.UserRoles
import java.sql.ResultSet

class PermissionData(config: DatabaseConfig) : CoVaDatabase(config) {

    fun getUserRoles(userInfo: UserInfo): UserRoles {
        val sql = "select * from userroles where userid = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, userInfo.id)
            val ur = UserRoles()
            ur.userInfo = userInfo
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    ur.roles.add(result2Role(rs))
                }
            }
            return ur
        }
    }

    private fun result2Role(rs: ResultSet): Role {
        val role = Role()
        with(role) {
            id = rs.getInt("id")
            name = rs.getString("name")
            description = rs.getString("description")
        }
        return role
    }

    fun getRolePermissions(roleId: Int): Set<Permission> {
        val permissions = getAllPermissions()
        val sql = "select permissionid from rolepermissions where roleid = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setInt(1, roleId)
            val permissionIds = mutableListOf<Int>()
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    permissionIds.add(rs.getInt("permissionid"))
                }
            }
            return permissions.filter { permissionIds.contains(it.id) }.toSet()
        }
    }

    private fun result2Permission(rs: ResultSet): Permission {
        val permission = Permission()
        with(permission) {
            id = rs.getInt("id")
            name = rs.getString("name")
            description = rs.getString("description")
            parentId = rs.getInt("parentid")
        }
        return permission
    }

    private lateinit var permissionSet: Set<Permission>
    private fun getAllPermissions(): Set<Permission> {
        if (!this::permissionSet.isInitialized) {
            val sql = "select * from permissions"
            connection.prepareStatement(sql).use { stmt ->
                val ret = mutableSetOf<Permission>()
                stmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        ret.add(result2Permission(rs))
                    }
                }
                permissionSet = ret.map { p ->
                    p.parent = ret.find { p.parentId == it.id }
                    p
                }.toSet()
            }
        }
        return permissionSet
    }

    fun getUserPermissions(userInfo: UserInfo): UserPermissions {
        val ur = getUserRoles(userInfo)
        val ret = UserPermissions()
        ret.userInfo = userInfo
        ret.roles.addAll(ur.roles)
        val permissions = mutableListOf<Permission>()
        ur.roles.forEach {
            val rolePermissions = getRolePermissions(it.id)
            permissions.addAll(rolePermissions)
        }
        val pIds = mutableListOf<Int>()
        val sql = "select * from userpermissions where userid = ?"
        connection.prepareStatement(sql).use { stmt ->
            stmt.setLong(1, userInfo.id)
            stmt.executeQuery().use { rs ->
                while (rs.next()) {
                    pIds.add(rs.getInt("permissionid"))
                }
            }
        }
        val allPermissions = getAllPermissions()
        pIds.removeIf { permissions.any { p -> p.id == it } }
        permissions.addAll(allPermissions.filter { pIds.contains(it.id) })
        ret.permissions = permissions.distinctBy { it.id }.toSet()
        return ret
    }
}
