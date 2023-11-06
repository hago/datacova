/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.action;

import com.hagoapp.datacova.utility.JsonStringify;

import java.util.HashMap;
import java.util.Map;

public class TaskActionExtra implements JsonStringify {
    private boolean continueNextWhenError = false;

    private Map<String, Object> misc = new HashMap<>();

    public boolean getContinueNextWhenError() {
        return continueNextWhenError;
    }

    public void setContinueNextWhenError(boolean continueNextWhenError) {
        this.continueNextWhenError = continueNextWhenError;
    }

    public Map<String, Object> getMisc() {
        return misc;
    }

    public void setMisc(Map<String, Object> misc) {
        this.misc = misc;
    }
}
