/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.config.ExecutorConfig;
import com.hagoapp.datacova.executor.Status;

import java.util.List;

public class ExecutorStatus implements JsonStringify {
    private ExecutorConfig executor;
    private List<Status> executions;

    public ExecutorConfig getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorConfig executor) {
        this.executor = executor;
    }

    public List<Status> getExecutions() {
        return executions;
    }

    public void setExecutions(List<Status> executions) {
        this.executions = executions;
    }

    public static ExecutorStatus fromJson(String json) {
        var token = new TypeToken<ExecutorStatus>() {};
        return new Gson().fromJson(json, token.getType());
    }
}
