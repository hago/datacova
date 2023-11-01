/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.ssh;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HostKeyItem that = (HostKeyItem) o;

        if (!Objects.equals(host, that.host)) return false;
        if (!Objects.equals(type, that.type)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
