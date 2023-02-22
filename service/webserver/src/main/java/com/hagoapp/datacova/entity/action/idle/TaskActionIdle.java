/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.idle;

import com.hagoapp.datacova.entity.action.TaskAction;

public class TaskActionIdle extends TaskAction {

    public static final int TASK_ACTION_TYPE_IDLE = 0;
    private Configuration configuration;
    private boolean executeResult = true;
    private String failReason = "Idle Action Fail";

    public TaskActionIdle() {
        super();
        type = TASK_ACTION_TYPE_IDLE;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public boolean getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(boolean executeResult) {
        this.executeResult = executeResult;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    @Override
    public int getType() {
        return TASK_ACTION_TYPE_IDLE;
    }
}
