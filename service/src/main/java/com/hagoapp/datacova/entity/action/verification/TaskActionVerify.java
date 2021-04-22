/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.verification;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.entity.action.TaskAction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    @SuppressWarnings("unchecked")
    public void loadActualContent(Map<String, Object> content) throws CoVaException {
        System.out.println("loadActualContent");
        System.out.println(content);
        List<Map<String, Object>> list = (List<Map<String, Object>>) content.get("configurations");
        configurations = list.stream().map(ConfigurationFactory::createConfiguration).collect(Collectors.toList());
        for (var conf : configurations) {
            if (!conf.isValid()) {
                throw new CoVaException(String.format("verify configuration is not valid: %s", conf.toJson()));
            }
        }
    }
}
