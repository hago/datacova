/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.verification;

import com.hagoapp.datacova.entity.action.TaskAction;
import com.hagoapp.datacova.entity.action.TaskActionType;

import java.util.List;

public class TaskActionVerify extends TaskAction {
    private List<Configuration> configurations;

    public TaskActionVerify() {
        super();
        type = TaskActionType.Verify;
    }

    public List<Configuration> getConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<Configuration> configurations) {
        this.configurations = configurations;
    }

    @Override
    public TaskActionType getType() {
        return TaskActionType.Verify;
    }
}
