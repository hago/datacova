/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.distribute.conf;

import com.hagoapp.datacova.entity.action.distribute.Configuration;

public class S3Config extends Configuration {
    public static final String DISTRIBUTION_TYPE_S3 = "s3";

    public static class Credential {
        private String accessKey;
        private String secret;
        private String token;

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    private String bucket;
    private boolean createBucketIfAbsent = false;
    private String region;
    private Credential credential;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean isCreateBucketIfAbsent() {
        return createBucketIfAbsent;
    }

    public void setCreateBucketIfAbsent(boolean createBucketIfAbsent) {
        this.createBucketIfAbsent = createBucketIfAbsent;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Credential getCredential() {
        return credential;
    }

    public void setCredential(Credential credential) {
        this.credential = credential;
    }

    @Override
    public String getType() {
        return DISTRIBUTION_TYPE_S3;
    }
}
