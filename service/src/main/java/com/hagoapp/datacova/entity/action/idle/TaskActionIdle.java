/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.idle;

import com.hagoapp.datacova.entity.action.TaskAction;
import org.apache.poi.ss.formula.eval.NotImplementedException;

import java.util.Map;

public class TaskActionIdle extends TaskAction {
    private Configuration configuration;
    private boolean result = true;
    private String failReason = "Idle Action Fail";

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
    public void load(Map<String, Object> map) {
        throw new NotImplementedException("");
    }
}
