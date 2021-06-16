/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config;

public class LdapConfig {
    private String host;
    private int port = 389;
    private String baseDistinguishedName;
    private String bindDistinguishedName;
    private String bindPassword;
    private String userFilter;
    private String groupFilter;
    private boolean ssl;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBaseDistinguishedName() {
        return baseDistinguishedName;
    }

    public void setBaseDistinguishedName(String baseDistinguishedName) {
        this.baseDistinguishedName = baseDistinguishedName;
    }

    public String getBindDistinguishedName() {
        return bindDistinguishedName;
    }

    public void setBindDistinguishedName(String bindDistinguishedName) {
        this.bindDistinguishedName = bindDistinguishedName;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public void setBindPassword(String bindPassword) {
        this.bindPassword = bindPassword;
    }

    public String getUserFilter() {
        return userFilter;
    }

    public void setUserFilter(String userFilter) {
        this.userFilter = userFilter;
    }

    public String getGroupFilter() {
        return groupFilter;
    }

    public void setGroupFilter(String groupFilter) {
        this.groupFilter = groupFilter;
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }
}
