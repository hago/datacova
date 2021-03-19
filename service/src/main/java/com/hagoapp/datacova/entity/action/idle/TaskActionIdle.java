/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.idle;

import com.hagoapp.datacova.entity.action.TaskAction;
import com.hagoapp.datacova.execution.executor.BaseTaskActionExecutor;

public class TaskActionIdle extends TaskAction {

    private static final int IDLE_ACTION_TYPE = -1;
    private Configuration configuration;
    private boolean result = true;
    private String failReason = "Idle Action Fail";

    public TaskActionIdle() {
        super();
        type = IDLE_ACTION_TYPE;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    @Override
    public int getType() {
        return IDLE_ACTION_TYPE;
    }

    @Override
    public Class<? extends TaskAction> getActionClass() {
        return TaskActionIdle.class;
    }

    @Override
    public Class<? extends BaseTaskActionExecutor> getExecutorClass() {
        return super.getExecutorClass();
    }
}
