/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.connection;

import com.fasterxml.jackson.jr.ob.JSON;
import com.hagoapp.datacova.JsonStringify;

import java.io.IOException;
import java.util.Map;

public class ConnectionConfig implements JsonStringify {
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Map<String, Object> toMap() throws IOException {
        return JSON.std.mapFrom(this.toJson());
    }
}
