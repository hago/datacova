/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.distribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Distributor {

    protected TaskActionDistribute distAction;
    protected final Logger logger = LoggerFactory.getLogger(Distributor.class);

    public void init(TaskActionDistribute action) {
        distAction = action;
    }

    protected Distributor() {
    }

    public abstract void distribute(String source);

    public abstract String supportedDistributionType();
}
