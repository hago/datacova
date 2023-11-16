/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.worker;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Application {

    @CommandLine.Option(names = {"-c", "--config"})
    private static String configFile;
    private static final Application app = new Application();
    private Config config;
    private final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        app.config = app.loadConfig(configFile);
        if (app.config == null) {
            return;
        }
        var cli = new CommandLine(app);
        cli.setExecutionStrategy(app::executionStrategy).execute(args);
    }

    public Config getConfig() {
        return config;
    }

    public static Application application() {
        return app;
    }

    private int executionStrategy(CommandLine.ParseResult parseResult) {
        return new CommandLine.RunLast().execute(parseResult); // default execution strategy
    }

    private static final String DEFAULT_CONFIG_PATH = "./worker.conf";

    private Config loadConfig(String path) {
        String fn;
        if ((path == null) || !new File(path).exists()) {
            var f = new File(DEFAULT_CONFIG_PATH);
            if (!f.exists()) {
                throw new UnsupportedOperationException(
                        String.format("config file not found, from neither %s nor %s.", path, f.getAbsolutePath()));
            }
            fn = DEFAULT_CONFIG_PATH;
        } else {
            fn = path;
        }
        try (var it = new FileInputStream(fn)) {
            return new Gson().fromJson(new String(it.readAllBytes()), Config.class);
        } catch (IOException e) {
            logger.error("config file loading failed: {}, exit.", fn);
            return null;
        }
    }

}
