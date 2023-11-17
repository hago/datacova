/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker;

import com.hagoapp.datacova.lib.data.DatabaseConfig;
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
    private int port;
    private String authKey;
    private DatabaseConfig db;
    private String fileStore;

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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public DatabaseConfig getDb() {
        return db;
    }

    public void setDb(DatabaseConfig db) {
        this.db = db;
    }

    public String getFileStore() {
        return fileStore;
    }

    public void setFileStore(String fileStore) {
        this.fileStore = fileStore;
    }
}
