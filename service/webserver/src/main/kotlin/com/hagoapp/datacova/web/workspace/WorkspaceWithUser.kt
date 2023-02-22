/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.workspace

import com.hagoapp.datacova.entity.workspace.WorkSpace
import com.hagoapp.datacova.entity.workspace.WorkSpaceUserRole
import com.hagoapp.datacova.user.UserInfo

data class WorkspaceWithUser(
    val workspace: WorkSpace,
    val owner: UserInfo,
    val users: List<WorkspaceUser>
)

data class WorkspaceUser(
    val user: UserInfo,
    val roles: List<WorkSpaceUserRole>
)
