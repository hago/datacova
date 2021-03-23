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
    private F2TConfig ingestOptions = new F2TConfig();

    public TaskActionIngest() {
        super();
        type = TaskActionType.DatabaseIngest;
        ingestOptions.setBatchColumnName("BatchId");
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public F2TConfig getIngestOptions() {
        return ingestOptions;
    }

    public void setIngestOptions(F2TConfig ingestOptions) {
        this.ingestOptions = ingestOptions;
    }

    @Override
    public TaskActionType getType() {
        return TaskActionType.DatabaseIngest;
    }
}
