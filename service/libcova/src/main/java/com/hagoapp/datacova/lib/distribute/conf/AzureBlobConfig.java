/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.distribute.conf;

import com.hagoapp.datacova.lib.distribute.Configuration;

public class AzureBlobConfig extends Configuration {

    public static final String DISTRIBUTION_TYPE_AZURE_BLOB = "blob";

    private String containerName;
    private String blobName;
    private String blobSasUrl;

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

    public String getBlobSasUrl() {
        return blobSasUrl;
    }

    public void setBlobSasUrl(String blobSasUrl) {
        this.blobSasUrl = blobSasUrl;
    }

    @Override
    public String getType() {
        return DISTRIBUTION_TYPE_AZURE_BLOB;
    }
}
