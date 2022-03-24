package com.hagoapp.datacova.distribute.conf;

import com.hagoapp.datacova.distribute.Configuration;

public class FtpConfig extends Configuration {

    public static final String ANONYMOUS_LOGIN = "anonymous";
    public static final String DISTRIBUTION_TYPE_FTP = "ftp";

    private String host;
    private int port = 21;
    private String login = ANONYMOUS_LOGIN;
    private String password = "";
    private String remotePath;
    private String remoteName;
    private boolean passive = false;
    private boolean binaryTransport = true;

    public FtpConfig() {
        super();
        this.type = DISTRIBUTION_TYPE_FTP;
    }

    public String getLogin() {
        return login == null || login.isBlank() ? ANONYMOUS_LOGIN : login;
    }

    public void setLogin(String value) {
        login = value;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRemotePath() {
        return remotePath;
    }

    public void setRemotePath(String remotePath) {
        this.remotePath = remotePath;
    }

    public String getRemoteName() {
        return remoteName;
    }

    public void setRemoteName(String remoteName) {
        this.remoteName = remoteName;
    }

    public boolean isPassive() {
        return passive;
    }

    public void setPassive(boolean passive) {
        this.passive = passive;
    }

    public boolean isBinaryTransport() {
        return binaryTransport;
    }

    public void setBinaryTransport(boolean binaryTransport) {
        this.binaryTransport = binaryTransport;
    }
}
