/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config;

import com.hagoapp.datacova.utility.Utils;

public class FileStorageConfig {
    private String uploadDirectory;
    private String thumbnailDirectory;
    private String tempDirectory;
    private String sshDirectory;

    public String getUploadDirectory() {
        return uploadDirectory;
    }

    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public String getThumbnailDirectory() {
        return thumbnailDirectory;
    }

    public void setThumbnailDirectory(String thumbnailDirectory) {
        this.thumbnailDirectory = thumbnailDirectory;
    }

    public String getTempDirectory() {
        return tempDirectory != null ? tempDirectory : Utils.getSystemTemporaryDirectory();
    }

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory != null ? tempDirectory : Utils.getSystemTemporaryDirectory();
    }

    public String getSshDirectory() {
        return sshDirectory;
    }

    public void setSshDirectory(String sshDirectory) {
        this.sshDirectory = sshDirectory;
    }
}
