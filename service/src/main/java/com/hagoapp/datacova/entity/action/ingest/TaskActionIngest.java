/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.ingest;

import com.hagoapp.datacova.entity.action.TaskAction;
import com.hagoapp.datacova.entity.action.TaskActionType;
import com.hagoapp.f2t.F2TConfig;

public class TaskActionIngest extends TaskAction {

    private int connectionId;
    private F2TConfig file2TableConfig;

    public TaskActionIngest() {
        super();
        type = TaskActionType.DatabaseIngest;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public F2TConfig getFile2TableConfig() {
        return file2TableConfig;
    }

    public void setFile2TableConfig(F2TConfig file2TableConfig) {
        this.file2TableConfig = file2TableConfig;
    }

    @Override
    public TaskActionType getType() {
        return TaskActionType.DatabaseIngest;
    }
}
