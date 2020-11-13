package com.hagoapp.datacova;

import com.hagoapp.datacova.config.CoVaConfig;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
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
        }
        return logger;
    }

    private static void initDefaultLogger() {
        logger = LoggerContext.getContext().getRootLogger();
        Layout<? extends Serializable> layout = PatternLayout.newBuilder().withAlwaysWriteExceptions(true)
                .withPattern("%d{DEFAULT_MICROS}\t%-5level\t%msg%n")
                .withConfiguration(LoggerContext.getContext().getConfiguration()).build();
        logger.getAppenders().values().forEach(appender -> {
            logger.removeAppender(appender);
        });
        ConsoleAppender appender = ConsoleAppender.createDefaultAppenderForLayout(layout);
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.ALL);
    }

    private static void initLogger() {
        //TODO
    }
}
