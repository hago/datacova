/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.connection;

import com.hagoapp.datacova.data.IDatabaseConnection;
import com.hagoapp.datacova.data.connection.PostgreSQLConnection;

public class PostgreSQLConfig extends ConnectionConfig {
    private String host;
    private int port = 5432;
    private String userName;
    private String password;
    private String dbName;

    public PostgreSQLConfig() {
        this.dbType = 0;
    }

    public int getPort() {
        return port <= 0 ? 5432 : port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    @Override
    public int getDbType() {
        return 0;
    }

    @Override
    public Class<? extends IDatabaseConnection> getConnectionClass() {
        return PostgreSQLConnection.class;
    }
}
