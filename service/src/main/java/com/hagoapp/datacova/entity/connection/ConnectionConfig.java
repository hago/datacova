/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.connection;

import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.data.IDatabaseConnection;

public class ConnectionConfig implements JsonStringify {
    protected int dbType;

    public int getDbType() {
        return dbType;
    }

    public Class<? extends IDatabaseConnection> getConnectionClass() {
        return null;
    }

}
