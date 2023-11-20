/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.worker;

import com.google.gson.Gson;
import com.hagoapp.datacova.worker.cli.DbExecutionCmd;
import com.hagoapp.datacova.worker.cli.ExecuteCmd;
import com.hagoapp.datacova.worker.cli.WorkerCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@CommandLine.Command(name = "worker", subcommands = {ExecuteCmd.class, DbExecutionCmd.class, WorkerCmd.class})
public class Application {

    private static final String DEFAULT_CONFIG_PATH = "./worker.conf";

    @CommandLine.Option(names = {"-c", "--config"}, defaultValue = DEFAULT_CONFIG_PATH)
    private String configFile;
    private static Application app;
    private Config config;
    private final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        app = oneApp();
        var cli = new CommandLine(app);
        cli.setExecutionStrategy(app::executionStrategy).execute(args);
    }

    public Config getConfig() {
        if (config == null) {
            config = loadConfig();
        }
        return config;
    }

    public static synchronized Application oneApp() {
        if (app == null) {
            app = new Application();
        }
        return app;
    }

    private Application() {
    }

    private int executionStrategy(CommandLine.ParseResult parseResult) {
        return new CommandLine.RunLast().execute(parseResult); // default execution strategy
    }

    private Config loadConfig() {
        if ((configFile == null) || !new File(configFile).exists()) {
            logger.error("config file not found, from {}.", configFile);
            System.exit(-1);
            return null;
        }
        try (var it = new FileInputStream(configFile)) {
            return new Gson().fromJson(new String(it.readAllBytes()), Config.class);
        } catch (IOException e) {
            logger.error("config file loading failed: {}, exit.", configFile);
            System.exit(-2);
            return null;
        }
    }

}
