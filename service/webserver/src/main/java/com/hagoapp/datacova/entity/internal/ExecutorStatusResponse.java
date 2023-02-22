/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.internal;

public class ExecutorStatusResponse {

    private int code;
    private ExecutorStatus data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public ExecutorStatus getData() {
        return data;
    }

    public void setData(ExecutorStatus data) {
        this.data = data;
    }
}
