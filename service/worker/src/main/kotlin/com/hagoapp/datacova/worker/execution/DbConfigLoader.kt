/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.execution

import com.hagoapp.datacova.lib.data.TaskExecutionData
import com.hagoapp.datacova.worker.Application
import com.hagoapp.f2t.database.config.DbConfig
import com.hagoapp.f2t.database.config.DbConfigReader

object DbConfigLoader {

    fun interface DbConfigProvider {
        fun lookup(id: Int): DbConfig?
    }

    var provider: DbConfigProvider = DbConfigProvider {
        val str = TaskExecutionData(Application.oneApp().config.db).getIngestDbConfigString(it)
            ?: return@DbConfigProvider null
        return@DbConfigProvider DbConfigReader.json2DbConfig(str)
    }

}
