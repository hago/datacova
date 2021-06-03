/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config;

import java.util.ArrayList;
import java.util.List;

public class BaseWebConfig {
    protected String bindIp = "127.0.0.1";
    protected int port = 6786;
    protected boolean allowCrossOriginResourceSharing = false;
    protected List<String> crossOriginSources = new ArrayList<>();
    protected List<String> privilegedIpAddresses = List.of(
            "127.0.0.1",
            "192.168.0.0/24",
            "10.*.*.*"
    );
    protected String tempDirectory = "./";
    protected String uploadTempDirectory = "./";
    protected long uploadSizeLimit = -1;
    protected boolean outputStackTrace = true;
    protected String baseUrl;

    public String getBindIp() {
        return bindIp;
    }

    public void setBindIp(String bindIp) {
        this.bindIp = bindIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isAllowCrossOriginResourceSharing() {
        return allowCrossOriginResourceSharing;
    }

    public void setAllowCrossOriginResourceSharing(boolean allowCrossOriginResourceSharing) {
        this.allowCrossOriginResourceSharing = allowCrossOriginResourceSharing;
    }

    public List<String> getCrossOriginSources() {
        return crossOriginSources;
    }

    public void setCrossOriginSources(List<String> crossOriginSources) {
        this.crossOriginSources = crossOriginSources;
    }

    public List<String> getPrivilegedIpAddresses() {
        return privilegedIpAddresses;
    }

    public void setPrivilegedIpAddresses(List<String> privilegedIpAddresses) {
        this.privilegedIpAddresses = privilegedIpAddresses;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public String getUploadTempDirectory() {
        return uploadTempDirectory;
    }

    public void setUploadTempDirectory(String uploadTempDirectory) {
        this.uploadTempDirectory = uploadTempDirectory;
    }

    public long getUploadSizeLimit() {
        return uploadSizeLimit;
    }

    public void setUploadSizeLimit(long uploadSizeLimit) {
        this.uploadSizeLimit = uploadSizeLimit;
    }

    public boolean isOutputStackTrace() {
        return outputStackTrace;
    }

    public void setOutputStackTrace(boolean outputStackTrace) {
        this.outputStackTrace = outputStackTrace;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String toString() {
        return "BaseWebConfig{" +
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
                '}';
    }
}
