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

class WorkspaceUserRoleUtil {
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
    }
}
