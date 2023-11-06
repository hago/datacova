/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility;

/**
 * Exception to present anomaly situation given by DataCoVa itself.
 */
public class CoVaException extends Exception {

    public CoVaException() {
        super();
    }

    public CoVaException(String message) {
        super(message);
    }

    public CoVaException(String message, Throwable cause) {
        super(message, cause);
    }

}
