/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.cli;

import com.hagoapp.datacova.lib.data.TaskExecutionData;
import com.hagoapp.datacova.lib.execution.ExecutionActionDetail;
import com.hagoapp.datacova.lib.execution.ExecutionDetail;
import com.hagoapp.datacova.lib.execution.TaskExecution;
import com.hagoapp.datacova.utility.StackTraceWriter;
import com.hagoapp.datacova.worker.Application;
import com.hagoapp.datacova.worker.Config;
import com.hagoapp.datacova.worker.Worker;
import com.hagoapp.datacova.worker.execution.TaskExecutionWatcher;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "execdb", description = {"run a task execution loaded from database"})
public class DbExecutionCmd implements Callable<Integer>, TaskExecutionWatcher {
    @CommandLine.Option(
            names = {"-id", "--id"}, description = {"task execution id"}, required = true
    )
    private int id;
    private final Logger logger = LoggerFactory.getLogger(DbExecutionCmd.class);
    private final Config config = Application.oneApp().getConfig();

    @Override
    public Integer call() {
        try {
            if (config == null) {
                logger.error("No config found, exit");
                return -1;
            }
            if (config.getDb() == null) {
                logger.error("No database config found, exit");
                return -2;
            }
            TaskExecution taskExecution;
            try (var db = new TaskExecutionData(config.getDb())) {
                taskExecution = db.getTaskExecution(id);
                if (taskExecution == null) {
                    logger.error("task execution with id {} not found, exit", id);
                    return -3;
                }
            }
            var worker = new Worker(taskExecution);
            worker.addWatcher(this);
            worker.execute();
            return 0;
        } catch (Exception ex) {
            logger.error("DbExecutionCmd error: {}", ex.getMessage());
            StackTraceWriter.write(ex, logger);
            return -10;
        }
    }

    /* Watcher for execution */
    @Override
    public void onStart(@NotNull TaskExecution te) {
        TaskExecutionWatcher.super.onStart(te);
    }

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

    @Override
    public void onActionStart(@NotNull TaskExecution te, int actionIndex) {
        //
    }

    @Override
    public void onActionComplete(@NotNull TaskExecution te, int actionIndex, @NotNull ExecutionActionDetail result) {
        //
    }

    @Override
    public void onActionError(@NotNull TaskExecution te, int actionIndex, @NotNull Exception error) {
        //
    }

    @Override
    public void onDataLoadStart(@NotNull TaskExecution te) {
        //
    }

    @Override
    public void onDataLoadComplete(@NotNull TaskExecution te, boolean isLoadingSuccessful) {
        //
    }
    /* End of watcher for execution */

}
