package com.hagoapp.datacova.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hagoapp.datacova.CoVaException;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configuration of DataCoVa.
 */
public class CoVaConfig {
    private static CoVaConfig config;

    public synchronized static CoVaConfig getConfig() {
        return config;
    }

    public static synchronized void loadConfig(String configFile) throws CoVaException {
        try (FileInputStream fis = new FileInputStream(configFile)) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                while (true) {
                    int i = fis.read(buffer, 0, buffer.length);
                    if (i < 0) {
                        break;
                    }
                    bos.write(buffer, 0, i);
                }
                String json = bos.toString(StandardCharsets.UTF_8);
                config = new Gson().fromJson(json, CoVaConfig.class);
            }
        } catch (IOException | JsonSyntaxException e) {
            throw new CoVaException(String.format("parse config file %s failed", configFile), e);
        }
    }

    private WebConfig web;
    private DatabaseConfig database;
    private LoggingConfig logging;

    public WebConfig getWeb() {
        return web;
    }

    public void setWeb(WebConfig web) {
        this.web = web;
    }

    public DatabaseConfig getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseConfig database) {
        this.database = database;
    }

    public LoggingConfig getLogging() {
        return logging;
    }

    public void setLogging(LoggingConfig logging) {
        this.logging = logging;
    }
}
