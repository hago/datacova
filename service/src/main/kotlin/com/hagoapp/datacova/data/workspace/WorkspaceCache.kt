/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.RedisCacheReader
import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole.Admin
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole.Maintainer
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole.Loader

class WorkspaceCache {
    companion object {
        private const val WORKSPACE_INFO = "Workspace"
        private const val WORKSPACE_User_ROLE = "WorkspaceUserRoles"
        private const val My_WORKSPACE_INFO = "MyWorkspace"

        @JvmStatic
        fun getWorkspace(id: Int): WorkSpace? {
            return RedisCacheReader.readCachedData(
                WORKSPACE_INFO, 3600,
                object : RedisCacheReader.GenericLoader<WorkSpace?> {
                    override fun perform(vararg params: Any?): WorkSpace? {
                        return WorkSpaceData().getWorkSpace(params[0] as Int)
                    }
                }, WorkSpace::class.java, id
            )
        }

        @JvmStatic
        fun getWorkspaceUserInRoles(
            id: Int,
            roles: List<WorkSpaceUserRole> = listOf(Admin, Maintainer, Loader)
        ): List<WorkSpaceData.WorkspaceBasicUser> {
            val token = object : TypeToken<List<WorkSpaceData.WorkspaceBasicUser>>() {}
            val list = RedisCacheReader.readCachedData(
                WORKSPACE_User_ROLE, 3600,
                object : RedisCacheReader.GenericLoader<List<WorkSpaceData.WorkspaceBasicUser>> {
                    override fun perform(vararg params: Any?): List<WorkSpaceData.WorkspaceBasicUser> {
                        return WorkSpaceData().getWorkspaceUserIdList(params[0] as Int)
                    }
                }, token.type, id
            )
            return list!!.filter { u -> u.role in roles }
        }

        @JvmStatic
        fun getMyWorkSpaces(userId: Long): List<WorkSpace>? {
            val token = object : TypeToken<List<WorkSpace>>() {}
            return RedisCacheReader.readCachedData(
                My_WORKSPACE_INFO, 3600,
                object : RedisCacheReader.GenericLoader<List<WorkSpace>> {
                    override fun perform(vararg params: Any?): List<WorkSpace> {
                        return if (params.isEmpty()) listOf() else
                            WorkSpaceData(CoVaConfig.getConfig().database).getMyWorkSpaces(params[0] as Long)
                    }
                }, token.type, userId
            )
        }

        @JvmStatic
        fun clearWorkspaceUser(id: Int) {
            RedisCacheReader.clearData(WORKSPACE_User_ROLE, id)
        }
    }
}
