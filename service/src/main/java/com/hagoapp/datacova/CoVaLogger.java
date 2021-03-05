/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova;

import com.hagoapp.datacova.config.CoVaConfig;
import com.hagoapp.datacova.config.LoggingConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.RollingFileAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TimeBasedTriggeringPolicy;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

public class CoVaLogger {
    private static Logger logger;

    public static synchronized Logger getLogger() {
        if (logger == null) {
            if (CoVaConfig.getConfig() == null) {
                initDefaultLogger();
            } else {
                initLogger();
            }
            logger.info("logger configured");
        }
        return logger;
    }

    private static void initDefaultLogger() {
        logger = LoggerContext.getContext().getRootLogger();
        Layout<? extends Serializable> layout = PatternLayout.newBuilder().withAlwaysWriteExceptions(true)
                .withPattern("%d{DEFAULT_MICROS}\t%-5level\t%msg%n")
                .withConfiguration(LoggerContext.getContext().getConfiguration()).build();
        logger.getAppenders().values().forEach(appender -> logger.removeAppender(appender));
        ConsoleAppender appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.ALL);
    }

    private static void initLogger() {
        logger = LoggerContext.getContext().getRootLogger();
        LoggingConfig config = CoVaConfig.getConfig().getLogging();
        Layout<? extends Serializable> layout = PatternLayout.newBuilder().withAlwaysWriteExceptions(true)
                .withPattern(config.getMessagePattern())
                .withConfiguration(LoggerContext.getContext().getConfiguration()).build();
        logger.getAppenders().values().forEach(appender -> logger.removeAppender(appender));
        if (config.isLog2Console()) {
            ConsoleAppender appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
            appender.start();
            logger.addAppender(appender);
        }
        if (config.isLog2File()) {
            RollingFileAppender appender = RollingFileAppender.newBuilder()
                    .setName(config.getLogFilePath())
                    .withFileName(config.getLogFilePath())
                    .withFilePattern(config.getRollingLogFilePattern())
                    .withStrategy(DefaultRolloverStrategy.newBuilder().build())
                    .withPolicy(TimeBasedTriggeringPolicy.newBuilder().build())
                    .setLayout(layout)
                    .build();
            appender.start();
            logger.addAppender(appender);
        }
        logger.setLevel(Level.ALL);
        Level level = Level.getLevel(config.getLevel());
        if (level == null) {
            logger.error("Incorrect level value: {}, ALL is used", config.getLevel());
        } else {
            logger.info("log level is set to {}", level);
            logger.setLevel(level);
        }
    }
}
