/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.ingest;

import com.hagoapp.datacova.entity.action.TaskAction;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import java.util.Map;

public class TaskActionIngest extends TaskAction {
    private int connectionId;
    private String targetTable;
    private String targetSchema = "";

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
    public void load(Map<String, Object> map) {
        throw new NotImplementedException("");
    }
}
