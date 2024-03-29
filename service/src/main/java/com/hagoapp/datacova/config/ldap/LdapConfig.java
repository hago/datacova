/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config.ldap;

import com.hagoapp.datacova.JsonStringify;

public class LdapConfig implements JsonStringify {
    private String host;
    private int port = 389;
    private String baseDistinguishName;
    private String bindDistinguishName;
    private String bindPassword;
    private String userDnPattern;
    private String userFilter;
    private String groupFilter;
    private boolean ssl;
    private LdapAttributes attributes;

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

    public String getBaseDistinguishName() {
        return baseDistinguishName;
    }

    public void setBaseDistinguishName(String baseDistinguishName) {
        this.baseDistinguishName = baseDistinguishName;
    }

    public String getBindDistinguishName() {
        return bindDistinguishName;
    }

    public void setBindDistinguishName(String bindDistinguishName) {
        this.bindDistinguishName = bindDistinguishName;
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

    public String getUserDnPattern() {
        return userDnPattern;
    }

    public void setUserDnPattern(String userDnPattern) {
        this.userDnPattern = userDnPattern;
    }

    public LdapAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(LdapAttributes attributes) {
        this.attributes = attributes;
    }
}
