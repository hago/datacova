/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.data.workspace

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.DatabaseConfig
import com.hagoapp.datacova.data.CoVaDatabase
import com.hagoapp.datacova.entity.connection.ConnectionConfig

class ConnectionData(config: DatabaseConfig) : CoVaDatabase(config) {

    constructor() : this(CoVaConfig.getConfig().database)

    fun getWorkspaceConnections(workspaceId: Int): List<ConnectionConfig> {
        TODO()
    }
}
