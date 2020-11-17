/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.action;

import com.hagoapp.datacova.entity.action.idle.IdleConfig;

public class TaskActionIdle extends TaskAction {
    private IdleConfig configuration;
    private boolean result = true;
    private String failReason = "Idle Action Fail";

    public IdleConfig getConfiguration() {
        return configuration;
    }

    public void setConfiguration(IdleConfig configuration) {
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
}
