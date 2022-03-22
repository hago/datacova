/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web;

public class MethodName {
    public final static String GET = "GET";
    public final static String POST = "POST";
    public final static String PUT = "PUT";
    public final static String OPTIONS = "OPTIONS";
    public final static String HEAD = "HEAD";
    public final static String CONNECT = "CONNECT";
    public final static String DELETE = "DELETE";
    public final static String TRACE = "TRACE";

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
