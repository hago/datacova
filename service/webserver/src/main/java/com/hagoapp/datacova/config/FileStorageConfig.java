/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config;

import com.hagoapp.datacova.file.FileStore;
import com.hagoapp.datacova.file.FileStoreFactory;
import com.hagoapp.datacova.utility.Utils;

/**
 * Configuration of file storages.
 *
 * @author suncjs
 * @since 0.1
 */
public class FileStorageConfig {
    private String uploadFileStore;
    private String thumbnailFileStore;
    private String tempFileStore;
    private String sshFileStore;

    private static final String LOCAL_FS_SCHEME_TEMPLATE = "localFs:%s";

    public String getUploadFileStore() {
        return uploadFileStore != null ? uploadFileStore :
                String.format(LOCAL_FS_SCHEME_TEMPLATE, Utils.getSystemTemporaryDirectory());
    }

    public void setUploadFileStore(String uploadFileStore) {
        this.uploadFileStore = uploadFileStore;
    }

    public String getThumbnailFileStore() {
        return thumbnailFileStore != null ? thumbnailFileStore :
                String.format(LOCAL_FS_SCHEME_TEMPLATE, Utils.getSystemTemporaryDirectory());
    }

    public void setThumbnailFileStore(String thumbnailFileStore) {
        this.thumbnailFileStore = thumbnailFileStore;
    }

    public String getTempFileStore() {
        return tempFileStore != null ? tempFileStore :
                String.format(LOCAL_FS_SCHEME_TEMPLATE, Utils.getSystemTemporaryDirectory());
    }

    public void setTempFileStore(String tempFileStore) {
        this.tempFileStore = tempFileStore;
    }

    public String getSshFileStore() {
        return sshFileStore != null ? sshFileStore :
                String.format(LOCAL_FS_SCHEME_TEMPLATE, Utils.getSystemTemporaryDirectory());
    }

    public void setSshFileStore(String sshFileStore) {
        this.sshFileStore = sshFileStore;
    }

    public static FileStore createFileStore(String fileStoreConnectionString) {
        var store = FileStoreFactory.createFileStore(fileStoreConnectionString);
        if (store == null) {
            throw new UnsupportedOperationException("File store creation error: " + fileStoreConnectionString);
        }
        return store;
    }
}
