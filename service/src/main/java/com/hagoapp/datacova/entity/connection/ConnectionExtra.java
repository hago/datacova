/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.connection;

import com.google.gson.GsonBuilder;

public class ConnectionExtra {
    private boolean clearData = false;
    private boolean createTable = true;
    private boolean addBatch = true;

    public boolean isClearData() {
        return clearData;
    }

    public void setClearData(boolean clearData) {
        this.clearData = clearData;
    }

    public boolean isCreateTable() {
        return createTable;
    }

    public void setCreateTable(boolean createTable) {
        this.createTable = createTable;
    }

    public boolean isAddBatch() {
        return addBatch;
    }

    public void setAddBatch(boolean addBatch) {
        this.addBatch = addBatch;
    }

    public static ConnectionExtra fromJson(String json) {
        return new GsonBuilder().create().fromJson(json, ConnectionExtra.class);
    }
}
