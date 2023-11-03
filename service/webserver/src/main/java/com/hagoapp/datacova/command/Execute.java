/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.command;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.config.CoVaConfig;
import com.hagoapp.datacova.data.execution.TaskExecutionData;
import com.hagoapp.datacova.execution.TaskExecutionWatcher;
import com.hagoapp.datacova.execution.Worker;
import com.hagoapp.datacova.executor.ExecuteResultMailer;
import com.hagoapp.datacova.lib.execution.ExecutionActionDetail;
import com.hagoapp.datacova.lib.execution.ExecutionDetail;
import com.hagoapp.datacova.lib.execution.TaskExecution;
import com.hagoapp.datacova.util.StackTraceWriter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@CommandLine.Command(name = "exec", description = "execute a task")
public class Execute extends CommandWithConfig implements TaskExecutionWatcher {

    @CommandLine.Option(names = {"-i", "--id"}, description = "id of task execution to be executed", required = true)
    private int taskExecId;

    @CommandLine.Option(names = {"-l", "--l"}, description = "locale to be used", required = false)
    private String locale;

    private final Logger logger = LoggerFactory.getLogger(Execute.class);

    @Override
    public Integer call() throws CoVaException {
        CoVaConfig.loadConfig(configFile);
        try (TaskExecutionData db = new TaskExecutionData(CoVaConfig.getConfig().getDatabase())) {
            TaskExecution taskExecution = db.getTaskExecution(taskExecId);
            if (taskExecution == null) {
                logger.error("Task execution {} not found", taskExecId);
                return -1;
            }
            taskExecution.setDetail(null);
            if (locale != null) {
                logger.debug("using cli locale: {}", locale);
                var loc = Locale.forLanguageTag(locale);
                if (loc.toLanguageTag().equals("und")) {
                    logger.error("Unrecognized locale: {}", locale);
                    logger.info("options of locale include:");
                    var locales = Arrays.stream(Locale.getAvailableLocales())
                            .map(Locale::toLanguageTag)
                            .filter(s -> !s.equals("und") && !s.equals("nn"))
                            .collect(Collectors.joining(","));
                    logger.info(locales);
                    return -1;
                }
                logger.debug("get locale: {}", loc);
                taskExecution.getTask().getExtra().setLocale(loc);
            }
            Worker worker = new Worker(taskExecution);
            worker.addWatcher(this);
            worker.addWatcher(new ExecuteResultMailer());
            worker.execute();
            return super.call();
        }
    }

    @Override
    public void onStart(@NotNull TaskExecution te) {
        logger.info("Execution {} of task {} started", te.getId(), te.getTask().getId());
    }

    @Override
    public void onComplete(@NotNull TaskExecution te, @NotNull ExecutionDetail result) {
        logger.info("Execution {} of task {} completed: {}",
                te.getId(), te.getTask().getId(), result.isSucceeded());
    }

    @Override
    public void onError(@NotNull TaskExecution te, @NotNull Exception error) {
        logger.error("Execution {} of task {} detected error: {}",
                te.getId(), te.getTask().getId(), error.getMessage());
        StackTraceWriter.write(error, logger);
    }

    @Override
    public void onActionStart(@NotNull TaskExecution te, int actionIndex) {
        logger.info("Execution {} of task {} started action {}: {}",
                te.getId(), te.getTask().getId(), actionIndex, te.getTask().getActions().get(actionIndex).getName());
    }

    @Override
    public void onActionComplete(@NotNull TaskExecution te, int actionIndex, @NotNull ExecutionActionDetail result) {
        logger.info("Execution {} of task {} completed action {}: {}, action {}",
                te.getId(), te.getTask().getId(), actionIndex,
                te.getTask().getActions().get(actionIndex).getName(), result.isSucceeded() ? "succeeded" : "failed");
    }

    @Override
    public void onActionError(@NotNull TaskExecution te, int actionIndex, @NotNull Exception error) {
        logger.error("Execution {} of task {} is performing action {}: {}, error: {} occurs",
                te.getId(), te.getTask().getId(), actionIndex,
                te.getTask().getActions().get(actionIndex).getName(), error.getMessage());
        StackTraceWriter.write(error, logger);
    }

    @Override
    public void onDataLoadStart(@NotNull TaskExecution te) {
        logger.info("Execution {} of task {} started loading data", te.getId(), te.getTask().getId());
    }

    @Override
    public void onDataLoadComplete(@NotNull TaskExecution te, boolean isLoadingSuccessful) {
        logger.info("Execution {} of task {} completed loading data, {}", te.getId(), te.getTask().getId(),
                isLoadingSuccessful ? "succeeded" : "failed");
    }
}
