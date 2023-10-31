/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.ingest;

import com.hagoapp.datacova.entity.action.TaskAction;

public class TaskActionIngest extends TaskAction {

    public static final int TASK_ACTION_TYPE_INGEST = 1;

    private int connectionId;
    private IngestOptions ingestOptions = new IngestOptions();

    public TaskActionIngest() {
        super();
        type = TASK_ACTION_TYPE_INGEST;
        ingestOptions.setBatchColumnName("BatchId");
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public IngestOptions getIngestOptions() {
        return ingestOptions;
    }

    public void setIngestOptions(IngestOptions ingestOptions) {
        this.ingestOptions = ingestOptions;
    }

    @Override
    public int getType() {
        return TASK_ACTION_TYPE_INGEST;
    }
}
