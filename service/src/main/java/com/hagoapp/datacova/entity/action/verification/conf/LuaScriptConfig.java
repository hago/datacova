/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.action.verification.conf;

import com.hagoapp.datacova.entity.action.verification.Configuration;

public class LuaScriptConfig extends Configuration {
    private static final int LUA_SCRIPT_CONFIGURATION_TYPE = 5;
    private String snippet;

    public String getSnippet() {
        return snippet;
    }

    public LuaScriptConfig() {
        super();
        type = LUA_SCRIPT_CONFIGURATION_TYPE;
        setFieldsCountLimit(-1);
    }

    @Override
    public boolean isValid() {
        if (snippet == null) {
            return false;
        }
        return super.isValid();
    }

}
