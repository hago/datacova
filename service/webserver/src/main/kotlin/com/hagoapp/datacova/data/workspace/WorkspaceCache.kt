/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.google.gson.reflect.TypeToken
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole.*
import com.hagoapp.datacova.utility.redis.RedisCacheReader

class WorkspaceCache {
    companion object {
        private const val WORKSPACE_INFO = "Workspace"
        private const val WORKSPACE_USER_ROLE = "WorkspaceUserRoles"
        private const val MY_WORKSPACE_INFO = "MyWorkspace"

        @JvmStatic
        fun getWorkspace(id: Int): WorkSpace? {
            return RedisCacheReader.readCachedData(
                CoVaConfig.getConfig().redis,
                WORKSPACE_INFO,
                3600,
                { params -> WorkSpaceData(CoVaConfig.getConfig().database).getWorkSpace(params[0] as Int) },
                WorkSpace::class.java,
                id
            )
        }

        @JvmStatic
        fun getWorkspaceUserInRoles(
            id: Int,
            roles: List<WorkSpaceUserRole> = listOf(Admin, Maintainer, Loader)
        ): List<WorkSpaceData.WorkspaceBasicUser> {
            val token = object : TypeToken<List<WorkSpaceData.WorkspaceBasicUser>>() {}
            val list = RedisCacheReader.readCachedData(
                CoVaConfig.getConfig().redis,
                WORKSPACE_USER_ROLE,
                3600,
                { params -> WorkSpaceData(CoVaConfig.getConfig().database).getWorkspaceUserIdList(params[0] as Int) },
                token.type,
                id
            )
            return list!!.filter { u -> u.role in roles }
        }

        @JvmStatic
        fun getMyWorkSpaces(userId: Long): List<WorkSpace>? {
            val token = object : TypeToken<List<WorkSpace>>() {}
            return RedisCacheReader.readCachedData(
                CoVaConfig.getConfig().redis,
                MY_WORKSPACE_INFO, 3600,
                { params ->
                    if (params.isEmpty()) listOf() else
                        WorkSpaceData(CoVaConfig.getConfig().database).getMyWorkSpaces(params[0] as Long)
                }, token.type, userId
            )
        }

        @JvmStatic
        fun clearWorkspaceUser(id: Int) {
            RedisCacheReader.clearData(CoVaConfig.getConfig().redis, WORKSPACE_USER_ROLE, id)
        }

        @JvmStatic
        fun clearMyWorkspaces(userId: Long) {
            RedisCacheReader.clearData(CoVaConfig.getConfig().redis, MY_WORKSPACE_INFO, userId)
        }
    }
}
