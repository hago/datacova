/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.verification;

import com.hagoapp.datacova.entity.action.TaskAction;

import java.util.List;

public class TaskActionVerify extends TaskAction {
    public static final int TASK_ACTION_TYPE_VERIFY = 2;
    private List<Configuration> configurations;

    public TaskActionVerify() {
        super();
        type = TASK_ACTION_TYPE_VERIFY;
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    @Override
    public int getType() {
        return TASK_ACTION_TYPE_VERIFY;
    }
}
