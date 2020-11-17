/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.config;

public class LoggingConfig {
    private String logFilePath = "./cova.log";
    private String rollingLogFilePattern = "./cova.log.%d{yyyyMMdd}";
    private boolean log2Console = true;
    private boolean log2File = true;
    private String messagePattern = "%d{DEFAULT_MICROS}\\t%-5level\\t%msg%n";
    private String level = "All";

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public String getRollingLogFilePattern() {
        return rollingLogFilePattern;
    }

    public void setRollingLogFilePattern(String rollingLogFilePattern) {
        this.rollingLogFilePattern = rollingLogFilePattern;
    }

    public boolean isLog2Console() {
        return log2Console;
    }

    public void setLog2Console(boolean log2Console) {
        this.log2Console = log2Console;
    }

    public boolean isLog2File() {
        return log2File;
    }

    public void setLog2File(boolean log2File) {
        this.log2File = log2File;
    }

    public String getMessagePattern() {
        return messagePattern;
    }

    public void setMessagePattern(String messagePattern) {
        this.messagePattern = messagePattern;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
