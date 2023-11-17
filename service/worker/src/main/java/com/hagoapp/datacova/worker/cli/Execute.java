/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.cli;

import com.hagoapp.datacova.lib.execution.TaskExecution;
import com.hagoapp.datacova.worker.Application;
import com.hagoapp.datacova.worker.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "exec", description = {"run a task execution"})
public class Execute implements Callable<Integer> {
    @CommandLine.Option(
            names = {"-e", "--execution"}, description = {"file contains task execution information"}, required = true
    )
    private String teFile;
    private final Logger logger = LoggerFactory.getLogger(Execute.class);
    private final Config config = Application.oneApp().getConfig();
    private TaskExecution taskExecution;

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
        try (var fis = new FileInputStream(teFile)) {
            var json = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
            taskExecution = TaskExecution.loadFromJson(json);
        }
        return 0;
    }
}
