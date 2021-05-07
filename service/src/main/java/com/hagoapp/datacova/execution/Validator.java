/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution;

import com.hagoapp.datacova.entity.action.verification.Configuration;
import com.hagoapp.f2t.DataRow;

public abstract class Validator {

    protected Configuration config;

    public Validator withConfig(Configuration configuration) {
        this.config = configuration;
        return this;
    }

    public abstract int getSupportedVerificationType();

    public abstract void verify(DataRow row);
}
