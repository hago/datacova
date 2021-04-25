/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.data.redis.RedisConfig;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configuration of DataCoVa.
 */
public class CoVaConfig implements JsonStringify {
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
    private RedisConfig redis;
    private TaskExecutionConfig task;
    private MailConfig mail;

    public MailConfig getMail() {
        return mail;
    }

    public void setMail(MailConfig mail) {
        this.mail = mail;
    }

    public TaskExecutionConfig getTask() {
        return task;
    }

    public void setTask(TaskExecutionConfig task) {
        this.task = task;
    }

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

    public RedisConfig getRedis() {
        return redis;
    }

    public void setRedis(RedisConfig redis) {
        this.redis = redis;
    }
}
