/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoVaLogger {
    private static final Logger logger = LoggerFactory.getLogger(CoVaLogger.class.getPackageName());

    public static synchronized Logger getLogger() {
        return logger;
    }

}
