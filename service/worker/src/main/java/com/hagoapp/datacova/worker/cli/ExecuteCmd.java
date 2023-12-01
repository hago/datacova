/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.cli;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hagoapp.datacova.lib.execution.ExecutionDetail;
import com.hagoapp.datacova.lib.execution.TaskExecution;
import com.hagoapp.datacova.utility.StackTraceWriter;
import com.hagoapp.datacova.worker.Application;
import com.hagoapp.datacova.worker.Config;
import com.hagoapp.datacova.worker.Worker;
import com.hagoapp.datacova.worker.execution.DbConfigLoader;
import com.hagoapp.datacova.worker.execution.TaskExecutionWatcher;
import com.hagoapp.f2t.database.config.DbConfig;
import com.hagoapp.f2t.database.config.DbConfigReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "exec", description = {"run a task execution"})
public class ExecuteCmd implements Callable<Integer>, TaskExecutionWatcher {
    @CommandLine.Option(
            names = {"-e", "--execution"}, description = {"json file of task execution"}, required = true
    )
    private String teFile;
    @CommandLine.Option(
            names = {"-c", "--connections"},
            description = "json file of a map associates id and connection configurations used by the task execution"
    )
    private String consFile;
    private final Logger logger = LoggerFactory.getLogger(ExecuteCmd.class);
    private final Config config = Application.oneApp().getConfig();
    private final Map<Integer, DbConfig> dbConfigMap = new HashMap<>();

    @Override
    public Integer call() throws Exception {
        if (config == null) {
            logger.error("No config found, exit");
            return -1;
        }
        if (!new File(teFile).exists()) {
            logger.error("execution info file {] not found {}, exit", teFile);
            return -2;
        }
        TaskExecution taskExecution;
        try (var fis = new FileInputStream(teFile)) {
            var json = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
            taskExecution = TaskExecution.loadFromJson(json);
        }
        if (consFile != null) {
            try (var fis = new FileInputStream(consFile)) {
                var json = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
                var token = new TypeToken<Map<Integer, Object>>() {
                };
                var gson = new Gson();
                Map<Integer, Object> map = gson.fromJson(json, token.getType());
                for (var entry : map.entrySet()) {
                    var s = gson.toJson(entry.getValue());
                    var cfg = DbConfigReader.json2DbConfig(s);
                    dbConfigMap.put(entry.getKey(), cfg);
                }
            }
        }
        DbConfigLoader.INSTANCE.setProvider(dbLoader);
        var worker = new Worker(taskExecution);
        worker.addWatcher(this);
        worker.execute();
        return 0;
    }

    private final DbConfigLoader.DbConfigProvider dbLoader = new DbConfigLoader.DbConfigProvider() {
        @Nullable
        @Override
        public DbConfig lookup(int id) {
            return dbConfigMap.get(id);
        }
    };

    @Override
    public void onComplete(@NotNull TaskExecution te, @NotNull ExecutionDetail result) {
        if (result.isSucceeded()) {
            logger.info("SUCCESS");
        } else {
            logger.error("FAILED");
        }
        logger.info("detail: {}", result);
    }

    @Override
    public void onError(@NotNull TaskExecution te, @NotNull Exception error) {
        logger.error("execution failed");
        StackTraceWriter.write(error, logger);
    }
}
