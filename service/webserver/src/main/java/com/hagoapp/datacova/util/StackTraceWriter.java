/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.util;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackTraceWriter {
    public static List<String> write(Throwable e, Logger logger) {
        List<String> stacktrace = new ArrayList<>();
        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter writer = new PrintWriter(sw)) {
                e.printStackTrace(writer);
                stacktrace = Arrays.asList(sw.toString().split(System.lineSeparator()).clone());
            }
        } catch (IOException ignored) {
            //
        }
        stacktrace.forEach(logger::error);
        return stacktrace;
    }
}
