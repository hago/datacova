/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public interface JsonStringify {

    class GsonHolder {
        private GsonHolder() {
        }
        static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
    }

    default String toJson() {
        return GsonHolder.GSON.toJson(this);
    }
}
