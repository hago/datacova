/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.util.ssh;

/**
 * Item to present a line of known hosts file.
 *
 * @author suncjs
 * @since 0.5
 */
public class HostKeyItem {
    /**
     * host name.
     */
    private final String host;
    /**
     * key type. E.g., ecdsa-sha2-nistp256, ssh-rsa.
     */
    private final String type;
    /**
     * signature
     */
    private final String key;

    public String getHost() {
        return host;
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    public HostKeyItem(String host, String type, String key) {
        this.host = host;
        this.type = type;
        this.key = key;
    }
}
