/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.distribute;

import com.hagoapp.datacova.config.CoVaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class Distributor {

    protected TaskActionDistribute distAction;
    protected final Logger logger = LoggerFactory.getLogger(Distributor.class);
    protected CoVaConfig cfg = CoVaConfig.getConfig();

    public void init(TaskActionDistribute action) {
        distAction = action;
    }

    public Distributor() {
    }

    abstract public void distribute(String source);

    abstract public String supportedDistributionType();
}
