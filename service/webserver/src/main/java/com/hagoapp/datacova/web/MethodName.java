/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web;

public class MethodName {
    private MethodName() {
    }

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PUT = "PUT";
    public static final String OPTIONS = "OPTIONS";
    public static final String HEAD = "HEAD";
    public static final String CONNECT = "CONNECT";
    public static final String DELETE = "DELETE";
    public static final String TRACE = "TRACE";

    public static boolean isValidName(String input) {
        return (input != null) && (
                (GET.compareToIgnoreCase(input) == 0)
                        || (POST.compareToIgnoreCase(input) == 0)
                        || (PUT.compareToIgnoreCase(input) == 0)
                        || (DELETE.compareToIgnoreCase(input) == 0)
                        || (HEAD.compareToIgnoreCase(input) == 0)
                        || (OPTIONS.compareToIgnoreCase(input) == 0)
                        || (TRACE.compareToIgnoreCase(input) == 0)
                        || (CONNECT.compareToIgnoreCase(input) == 0)
        );
    }
}
