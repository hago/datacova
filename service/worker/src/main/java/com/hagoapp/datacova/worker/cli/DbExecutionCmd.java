/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.cli;

import com.hagoapp.datacova.lib.data.TaskExecutionData;
import com.hagoapp.datacova.lib.execution.TaskExecution;
import com.hagoapp.datacova.lib.ingest.TaskActionIngest;
import com.hagoapp.datacova.worker.Application;
import com.hagoapp.datacova.worker.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "execdb", description = {"run a task execution loaded from database"})
public class DbExecutionCmd implements Callable<Integer> {
    @CommandLine.Option(
            names = {"-id", "--id"}, description = {"task execution id"}, required = true
    )
    private int id;
    private final Logger logger = LoggerFactory.getLogger(DbExecutionCmd.class);
    private final Config config = Application.oneApp().getConfig();
    private TaskExecution taskExecution;

    @Override
    public Integer call() throws Exception {
        if (config == null) {
            logger.error("No config found, exit");
            return -1;
        }
        if (config.getDb() == null) {
            logger.error("No database config found, exit");
            return -2;
        }
        try (var db = new TaskExecutionData(config.getDb())) {
            taskExecution = db.getTaskExecution(id);
            if (taskExecution == null) {
                logger.error("task execution with id {} not found, exit", id);
            }
        }
        return 0;
    }
}
