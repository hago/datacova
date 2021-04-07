/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.command;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.CoVaLogger;
import com.hagoapp.datacova.config.CoVaConfig;
import com.hagoapp.datacova.execution.Service;
import com.hagoapp.datacova.web.WebManager;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(name = "start", description = "start service")
public class Start extends CommandWithConfig {

    @Override
    public Integer call() throws CoVaException {
        CoVaConfig.loadConfig(configFile);
        Logger logger = CoVaLogger.getLogger();
        WebManager.getManager().createWebServer(CoVaConfig.getConfig().getWeb(), List.of(
                "com.hagoapp.datacova.web"
        ));
        logger.info("web server created");
        Service.Companion.getExecutor().startExecutionService();
        logger.info("builtin execution service created");
        return super.call();
    }
}
