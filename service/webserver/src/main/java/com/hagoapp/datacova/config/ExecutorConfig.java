/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config;

/**
 * This is the config to start executor service of application.
 */
public class ExecutorConfig extends WebConfig {
    private String dispatcherUrl;
    private String name;
    private String executorUrl;

    public String getDispatcherUrl() {
        return dispatcherUrl;
    }

    public void setDispatcherUrl(String dispatcherUrl) {
        this.dispatcherUrl = dispatcherUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExecutorUrl() {
        return executorUrl;
    }

    public void setExecutorUrl(String executorUrl) {
        this.executorUrl = executorUrl;
    }
}
