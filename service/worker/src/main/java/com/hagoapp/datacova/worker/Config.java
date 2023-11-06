/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker;

import com.hagoapp.datacova.utility.JsonStringify;

/**
 * The config for worker.
 *
 * @author suncjs
 * @since 0.5
 */
public class Config implements JsonStringify {
    /**
     * The group that this worker belongs.
     */
    private String group;
    /**
     * The web socket url to connect;
     */
    private String server;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
