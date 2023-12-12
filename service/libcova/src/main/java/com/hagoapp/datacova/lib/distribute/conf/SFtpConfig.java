/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.distribute.conf;

import com.hagoapp.datacova.lib.distribute.Configuration;
import com.hagoapp.datacova.lib.distribute.sftp.SFtpAuthType;

import java.util.Arrays;

/**
 * Configuration file for an sftp target.
 *
 * @author suncjs
 * @since 0.5
 */
public class SFtpConfig extends Configuration {

    private String host;
    private int port = 22;
    private String login;
    private String password;
    private String remotePath;
    private String remoteName;
    private SFtpAuthType authType = SFtpAuthType.PASSWORD;
    private String privateKeyFile;
    private byte[] privateKey;
    private String passPhrase;

    public static final String DISTRIBUTE_TYPE_SFTP = "sftp";

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

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {
        this.privateKey = privateKey;
    }

    public String getPrivateKeyFile() {
        return privateKeyFile;
    }

    public void setPrivateKeyFile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }

    public String getPassPhrase() {
        return passPhrase;
    }

    public void setPassPhrase(String passPhrase) {
        this.passPhrase = passPhrase;
    }

    @Override
    public String toString() {
        return "SFtpConfig{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                ", remotePath='" + remotePath + '\'' +
                ", remoteName='" + remoteName + '\'' +
                ", authType=" + authType +
                ", privateKeyFile='" + privateKeyFile + '\'' +
                ", privateKey=" + Arrays.toString(privateKey) +
                ", passPhrase='" + passPhrase + '\'' +
                ", copyOriginal=" + copyOriginal +
                ", overwriteExisted=" + overwriteExisted +
                ", targetFileName='" + targetFileName + '\'' +
                '}';
    }
}
