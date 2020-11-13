package com.hagoapp.datacova.config;

public class LoggingConfig {
    private String logFilePath = "./";
    private String rollingLogFilePattern;
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
