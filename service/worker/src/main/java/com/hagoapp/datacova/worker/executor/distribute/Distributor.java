/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.executor.distribute;

import com.hagoapp.datacova.lib.distribute.TaskActionDistribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Base class for distributor, this type of executor is to distribute data file to somewhere.
 *
 * @author suncjs
 * @since 0.1
 */
public abstract class Distributor {

    protected TaskActionDistribute distAction;
    protected final Logger logger = LoggerFactory.getLogger(Distributor.class);

    public void init(TaskActionDistribute action) {
        distAction = action;
    }

    protected Distributor() {
    }

    public abstract void distribute(InputStream src);

    public abstract String supportedDistributionType();
}
