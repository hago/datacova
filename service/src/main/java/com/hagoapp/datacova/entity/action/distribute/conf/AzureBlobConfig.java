/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.distribute.conf;

import com.hagoapp.datacova.entity.action.distribute.Configuration;

public class AzureBlobConfig extends Configuration {

    public static final String DISTRIBUTION_TYPE_AZURE_BLOB = "blob";

    private String sourceFileName;
    private String endPoint;
    private String token;
    private String containerName;
    private String blobName;

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getBlobName() {
        return blobName;
    }

    public void setBlobName(String blobName) {
        this.blobName = blobName;
    }

    @Override
    public String getType() {
        return DISTRIBUTION_TYPE_AZURE_BLOB;
    }
}
