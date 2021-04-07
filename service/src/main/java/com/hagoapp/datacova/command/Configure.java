/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.command;

import com.hagoapp.datacova.Application;
import com.hagoapp.datacova.CoVaLogger;
import com.hagoapp.datacova.config.WebConfig;
import com.hagoapp.datacova.web.WebManager;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "config", description = "create config file")
public class Configure implements Callable<Integer> {

    public static final String CONFIG_FILE_TO_WRITE = "CONFIG_FILE_TO_WRITE";

    @CommandLine.Option(names = {"--out", "-o"},
            description = "the generated config file name, default to 'config.yyyyMMdd.json'")
    private String outputFileName = String.format("config.%s.json",
            LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));

    @CommandLine.Option(names = {"--bind", "-b"}, description = "Bind Address", defaultValue = "127.0.0.1")
    private String bindIpAddress;

    @CommandLine.Option(names = {"--port", "-p"}, description = "Bind port", defaultValue = "8080",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private int port;

    private Logger logger;

    @Override
    public Integer call() throws Exception {
        Application.setData(CONFIG_FILE_TO_WRITE, outputFileName);
        WebConfig config = createConfigureWebConfig();
        WebManager.getManager().createWebServer(config, List.of(
                "com.hagoapp.datacova.configure",
                "com.hagoapp.datacova.web.default"
        ));
        System.out.printf("Please visit http://%s:%d to create config file.%n"
                , bindIpAddress, port);
        logger = CoVaLogger.getLogger();
        return 0;
    }

    private WebConfig createConfigureWebConfig() {
        WebConfig config = new WebConfig();
        config.setBindIp(bindIpAddress);
        config.setPort(port);
        return config;
    }
}
