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
import com.hagoapp.datacova.dispatcher.Dispatcher;
import com.hagoapp.datacova.executor.Executor;
import com.hagoapp.datacova.web.WebManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "start", description = "start service")
public class Start extends CommandWithConfig {

    private final Logger logger = LoggerFactory.getLogger(Start.class);

    @Override
    public Integer call() throws CoVaException {
        CoVaConfig.loadConfig(configFile);
        var config = CoVaConfig.getConfig();

        boolean runDispatcher = config.getWeb() != null;
        boolean runExecutor = config.getExecutor() != null;
        if (runDispatcher) {
            logger.info("Starting dispatcher functions");
            var packages = List.of("com.hagoapp.datacova.web");
            WebManager.getManager(config.getWeb(), packages);
            logger.info("Web server started");
            Dispatcher.Companion.get().startDispatcher();
            logger.info("Dispatcher started");
        }
        if (runExecutor) {
            logger.info("Starting executor functions");
            var packages = List.of("com.hagoapp.datacova.executor.web");
            WebManager.getManager(config.getExecutor(), packages);
            logger.info("Executor web API started");
            var executor = Executor.Companion.getExecutor();
            if (executor == null) {
                logger.error("Error executor creation");
            } else {
                executor.start();
            }
            logger.info("Executor service created");
        }
        return super.call();
    }
}
