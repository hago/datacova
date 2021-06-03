/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.config;

import java.util.List;

/**
 * Configuration of web interfaces.
 */
public class WebConfig extends BaseWebConfig {
    private List<WebSocketConfig> webSockets;
    private String privateKeyFile;

    public List<WebSocketConfig> getWebSockets() {
        return webSockets;
    }

    public void setWebSockets(List<WebSocketConfig> webSockets) {
        this.webSockets = webSockets;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public void setPrivateKeyFile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }

    @Override
    public String toString() {
        return "WebConfig{" +
                "bindIp='" + bindIp + '\'' +
                ", port=" + port +
                ", allowCrossOriginResourceSharing=" + allowCrossOriginResourceSharing +
                ", crossOriginSources=" + crossOriginSources +
                ", privilegedIpAddresses=" + privilegedIpAddresses +
                ", tempDirectory='" + tempDirectory + '\'' +
                ", uploadTempDirectory='" + uploadTempDirectory + '\'' +
                ", uploadSizeLimit=" + uploadSizeLimit +
                ", outputStackTrace=" + outputStackTrace +
                ", baseUrl='" + baseUrl + '\'' +
                ", webSockets=" + webSockets +
                ", privateKeyFile='" + privateKeyFile + '\'' +
                '}';
    }
}
