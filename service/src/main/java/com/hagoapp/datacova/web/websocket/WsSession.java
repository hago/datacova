/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket;

import com.hagoapp.datacova.user.UserInfo;
import com.hagoapp.datacova.util.Utils;

import java.time.Instant;
import java.util.Objects;

public class WsSession {
    private UserInfo userInfo;
    private String id;
    private String deviceIdentity;
    private final long establishedAt = Instant.now().toEpochMilli();
    private String remoteIp;

    public WsSession() {
        id = Utils.genRandomString(32, null);
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public void setRemoteIp(String remoteIp) {
        this.remoteIp = remoteIp;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceIdentity() {
        return deviceIdentity;
    }

    public void setDeviceIdentity(String deviceIdentity) {
        this.deviceIdentity = deviceIdentity == null ? String.format("Device %s", id) : deviceIdentity;
    }

    public long getEstablishedAt() {
        return establishedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WsSession session = (WsSession) o;

        if (!Objects.equals(userInfo, session.userInfo)) return false;
        if (!Objects.equals(id, session.id)) return false;
        return Objects.equals(deviceIdentity, session.deviceIdentity);
    }

    @Override
    public int hashCode() {
        int result = userInfo != null ? userInfo.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (deviceIdentity != null ? deviceIdentity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Session{" +
                "userInfo=" + userInfo +
                ", id='" + id + '\'' +
                ", device='" + deviceIdentity + '\'' +
                '}';
    }
}
