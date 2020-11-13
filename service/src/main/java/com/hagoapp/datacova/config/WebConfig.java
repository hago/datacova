package com.hagoapp.datacova.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration of web interfaces.
 */
public class WebConfig {
    private String bindIp = "127.0.0.1";
    private int port = 6786;
    private boolean allowCrossOriginResourceSharing = false;
    private List<String> crossOriginSources = new ArrayList<>();
    private List<String> privilegedIpAddresses = List.of(
            "127.0.0.1",
            "192.168.0.0/24",
            "10.*.*.*"
    );
    private String tempDirectory = "./";
    private String uploadTempDirectory = "./";
    private long uploadSizeLimit = -1;

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

    @Override
    public String toString() {
        return "WebConfig{"
                + "bindIp='" + bindIp + '\''
                + ", port=" + port
                + ", allowCrossOriginResourceSharing=" + allowCrossOriginResourceSharing
                + ", crossOriginSources=" + crossOriginSources
                + ", privilegedIpAddresses=" + privilegedIpAddresses
                + ", tempDirectory='" + tempDirectory + '\''
                + ", uploadTempDirectory='" + uploadTempDirectory + '\''
                + ", uploadSizeLimit=" + uploadSizeLimit + '}';
    }
}
