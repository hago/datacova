/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hagoapp.datacova.utility.CoVaException;
import com.hagoapp.datacova.utility.JsonStringify;
import com.hagoapp.datacova.utility.redis.RedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Configuration of DataCoVa.
 */
public class CoVaConfig implements JsonStringify {
    private static CoVaConfig config;
    private static final String DEFAULT_CONFIG = "config.json";
    private static final Logger logger = LoggerFactory.getLogger(CoVaConfig.class);

    public static synchronized CoVaConfig getConfig() {
        if (config == null) {
            try {
                loadConfig(DEFAULT_CONFIG);
            } catch (CoVaException e) {
                logger.error("CoVa config init error: {}", e.getMessage());
            }
        }
        return config;
    }

    public static synchronized void loadConfig(String configFile) throws CoVaException {
        try (var fis = new FileInputStream(configFile)) {
            try (var bos = new ByteArrayOutputStream()) {
                var buffer = new byte[1024];
                while (true) {
                    var i = fis.read(buffer, 0, buffer.length);
                    if (i < 0) {
                        break;
                    }
                    bos.write(buffer, 0, i);
                }
                var json = bos.toString(StandardCharsets.UTF_8);
                config = new Gson().fromJson(json, CoVaConfig.class);
                logger.debug("Loading config from {}", new File(configFile).getAbsolutePath());
                logger.info("CoVa config loaded: {}", config);
            }
        } catch (IOException | JsonSyntaxException e) {
            throw new CoVaException(String.format("parse config file %s failed", configFile), e);
        }
    }

    private WebConfig web;
    private DatabaseConfig database;
    private RedisConfig redis;
    private MailConfig mail;
    private TemplateConfig template;
    private FileStorageConfig fileStorage;
    private ExecutorConfig executor;

    public ExecutorConfig getExecutor() {
        return executor;
    }

    public void setExecutor(ExecutorConfig executor) {
        this.executor = executor;
    }

    public FileStorageConfig getFileStorage() {
        return fileStorage;
    }

    public void setFileStorage(FileStorageConfig fileStorage) {
        this.fileStorage = fileStorage;
    }

    public MailConfig getMail() {
        return mail;
    }

    public void setMail(MailConfig mail) {
        this.mail = mail;
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

    public RedisConfig getRedis() {
        return redis;
    }

    public void setRedis(RedisConfig redis) {
        this.redis = redis;
    }

    public TemplateConfig getTemplate() {
        return template;
    }

    public void setTemplate(TemplateConfig template) {
        this.template = template;
    }

    @Override
    public String toString() {
        return this.toJson();
    }
}
