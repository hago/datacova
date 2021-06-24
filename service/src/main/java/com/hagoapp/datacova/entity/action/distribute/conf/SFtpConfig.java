package com.hagoapp.datacova.entity.action.distribute.conf;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.distribute.sftp.HostKeyItem;
import com.hagoapp.datacova.distribute.sftp.SFtpAuthType;
import com.hagoapp.datacova.entity.action.distribute.Configuration;
import com.hagoapp.datacova.entity.action.distribute.IDistributeExtra;
import com.hagoapp.datacova.execution.distribute.sftp.KnownHostsStore;

public class SFtpConfig extends Configuration implements IDistributeExtra {

    private String host;
    private int port = 22;
    private String login;
    private String password;
    private String remotePath;
    private String remoteName;
    private SFtpAuthType authType = SFtpAuthType.Password;
    private HostKeyItem knownHost;
    private String publicKeyFile;

    public static final int DISTRIBUTE_TYPE_SFTP = 2;

    public SFtpConfig() {
        super();
        this.type = DISTRIBUTE_TYPE_SFTP;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
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

    public SFtpAuthType getAuthType() {
        return authType;
    }

    public void setAuthType(SFtpAuthType authType) {
        this.authType = authType;
    }

    public HostKeyItem getKnownHost() {
        return knownHost;
    }

    public void setKnownHost(HostKeyItem knownHost) {
        this.knownHost = knownHost;
    }

    public String getPublicKeyFile() {
        return publicKeyFile;
    }

    public void setPublicKeyFile(String publicKeyFile) {
        this.publicKeyFile = publicKeyFile;
    }

    @Override
    public void setExtraInformation() throws CoVaException {
        KnownHostsStore ks = KnownHostsStore.Companion.getStore();
        String key = String.format("[%s]:%d", host, port);
        knownHost = ks.getHostKeyItem(key);
        if ((knownHost == null) && (port == 22)) {
            knownHost = ks.getHostKeyItem(host);
        }
        if (knownHost == null) {
            throw new CoVaException(String.format("ssh key not found host %s with port %d", host, port));
        }
    }
}
