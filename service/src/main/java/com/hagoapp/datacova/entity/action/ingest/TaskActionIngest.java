/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.ingest;

import com.hagoapp.datacova.entity.action.TaskAction;
import com.hagoapp.datacova.execution.executor.BaseTaskActionExecutor;

public class TaskActionIngest extends TaskAction {

    private static final int INGEST_ACTION_TYPE = 0;

    private int connectionId;
    private String targetTable;
    private String targetSchema = "";

    public TaskActionIngest() {
        super();
        type = INGEST_ACTION_TYPE;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public String getTargetTable() {
        return targetTable;
    }

    public void setTargetTable(String targetTable) {
        this.targetTable = targetTable;
    }

    public String getTargetSchema() {
        return targetSchema;
    }

    public void setTargetSchema(String targetSchema) {
        this.targetSchema = targetSchema;
    }

    @Override
    public int getType() {
        return INGEST_ACTION_TYPE;
    }

    @Override
    public Class<? extends TaskAction> getActionClass() {
        return TaskActionIngest.class;
    }

    @Override
    public Class<? extends BaseTaskActionExecutor> getExecutorClass() {
        return super.getExecutorClass();
    }
}
