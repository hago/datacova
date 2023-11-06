/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.distribute;

import com.hagoapp.datacova.utility.CoVaException;
import com.hagoapp.datacova.lib.action.TaskAction;

import java.util.Map;

public class TaskActionDistribute extends TaskAction {
    public static final int TASK_ACTION_TYPE_DISTRIBUTE = 3;

    public TaskActionDistribute() {
        super();
        type = TASK_ACTION_TYPE_DISTRIBUTE;
    }

    private Configuration configuration;

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void loadActualContent(Map<String, Object> content) throws CoVaException {
        super.loadActualContent(content);
        Map<String, Object> conf = (Map<String, Object>) content.get("configuration");
        this.configuration = ConfigurationFactory.createConfiguration(conf);
    }
}
