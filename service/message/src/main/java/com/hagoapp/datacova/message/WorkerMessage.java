/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message;

/**
 * Interface for all message.
 *
 * @author suncjs
 * @since 0.5
 */
public @interface WorkerMessage {
    /**
     * Tells the message type.
     *
     * @return message type
     */
    int type();
}
