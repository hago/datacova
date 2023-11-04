/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.util;

import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A writer to print exception detail including stack traces to logger.
 *
 * @author suncjs
 * @since 0.1
 */
public class StackTraceWriter {
    /**
     * Method to logging stack trace.
     *
     * @param e      the exception
     * @param logger the logger to use
     * @return Strings of stack trace
     */
    public static List<String> write(Throwable e, Logger logger) {
        logger.error("Error {}", e.getMessage());
        for (var stackTrace : e.getStackTrace()) {
            logger.error("\t{}", stackTrace);
        }
        return Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.toList());
    }
}
