/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config;

public class ExecutorConfig extends BaseWebConfig {
    private String dispatcherUrl;
    private String publicKeyFile;

    public String getDispatcherUrl() {
        return dispatcherUrl;
    }

    public void setDispatcherUrl(String dispatcherUrl) {
        this.dispatcherUrl = dispatcherUrl;
    }

    public String getPublicKeyFile() {
        return publicKeyFile;
    }

    public void setPublicKeyFile(String publicKeyFile) {
        this.publicKeyFile = publicKeyFile;
    }
}
