/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StackTraceWriter {

    private StackTraceWriter() {}
    public static List<String> write(Throwable e, Logger logger) {
        List<String> stacktrace = new ArrayList<>();
        try (var sw = new StringWriter()) {
            try (var writer = new PrintWriter(sw)) {
                e.printStackTrace(writer);
                stacktrace = Arrays.asList(sw.toString().split(System.lineSeparator()).clone());
            }
        } catch (IOException ignored) {
            // ignore
        }
        stacktrace.forEach(logger::error);
        if (e.getCause() != null) {
            stacktrace.addAll(write(e.getCause(), logger));
        }
        return stacktrace;
    }
}
