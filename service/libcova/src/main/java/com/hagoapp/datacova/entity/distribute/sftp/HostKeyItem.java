/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.distribute.sftp;

public class HostKeyItem {
    private final String host;
    private final String type;
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
