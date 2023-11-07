/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.http;

import java.util.Arrays;

/**
 * Enumeration of http schemes.
 *
 * @author suncj2
 * @since 2.0
 */
public enum Scheme {
    HTTP("http"),
    HTTPS("https");

    private String value;

    Scheme(String protocolName) {
        value = protocolName.toLowerCase();
        if (Arrays.stream(Scheme.values()).noneMatch(protocol -> protocol.toString().equals(value))) {
            value = "https";
        }
    }

    public String getValue() {
        return value;
    }
}
