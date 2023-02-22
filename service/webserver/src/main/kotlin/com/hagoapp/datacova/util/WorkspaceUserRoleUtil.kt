/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.util

import com.hagoapp.datacova.data.workspace.WorkspaceCache
import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
import com.hagoapp.datacova.user.UserInfo

class WorkspaceUserRoleUtil(private val userId: Long, private val workspace: WorkSpace) {
    companion object {
        @JvmStatic
        fun isAdmin(user: UserInfo, workspace: WorkSpace): Boolean {
            return workspace.ownerId == user.id || WorkspaceCache.getWorkspaceUserInRoles(
                workspace.id, listOf(WorkSpaceUserRole.Admin)
            ).any { it.userid == user.id }
        }

        @JvmStatic
        fun isMaintainer(user: UserInfo, workspace: WorkSpace): Boolean {
            return WorkspaceCache.getWorkspaceUserInRoles(workspace.id, listOf(WorkSpaceUserRole.Maintainer))
                .any { it.userid == user.id }
        }

        @JvmStatic
        fun isLoader(user: UserInfo, workspace: WorkSpace): Boolean {
            return WorkspaceCache.getWorkspaceUserInRoles(workspace.id, listOf(WorkSpaceUserRole.Loader))
                .any { it.userid == user.id }
        }

        @JvmStatic
        fun isUser(user: UserInfo, workspaceId: Int): Boolean {
            return WorkspaceCache.getWorkspaceUserInRoles(workspaceId).any { it.userid == user.id }
        }

        @JvmStatic
        fun isAnyRolesOf(user: UserInfo, workspace: WorkSpace, roles: Set<WorkSpaceUserRole>): Boolean {
            val r = WorkspaceCache.getWorkspaceUserInRoles(workspace.id)
                .any { user.id == it.userid && it.role in roles }
            return if (WorkSpaceUserRole.Admin in roles) (r || (user.id == workspace.ownerId)) else r
        }
    }

    private val wkUserRoles = WorkspaceCache.getWorkspaceUserInRoles(workspace.id)

    fun isOwner(): Boolean {
        return userId == workspace.ownerId
    }

    fun isAdmin(): Boolean {
        return isOwner() || wkUserRoles.firstOrNull { it.userid == userId }?.role == WorkSpaceUserRole.Admin
    }

    fun isMaintainer(): Boolean {
        return wkUserRoles.firstOrNull { it.userid == userId }?.role == WorkSpaceUserRole.Maintainer
    }

    fun isLoader(): Boolean {
        return wkUserRoles.firstOrNull { it.userid == userId }?.role == WorkSpaceUserRole.Loader
    }

    fun isUser(): Boolean {
        return wkUserRoles.any { it.userid == userId }
    }
}
