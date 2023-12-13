/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova;

import com.hagoapp.datacova.command.Start;
import com.hagoapp.datacova.command.Stop;
import picocli.CommandLine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The entry of application. Based on command line arguments, user can start / stop services or execute a task.
 */
@CommandLine.Command(name = "DataCoVa", version = "0.1", description = "Service of DataCoVa", subcommands = {
        Start.class, Stop.class
})
public class Application {

    private static final Map<String, Object> internalData = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        var app = new Application();
        var cli = new CommandLine(app);
        cli.setExecutionStrategy(app::executionStrategy).execute(args);
    }

    public static void setData(String key, Object data) {
        internalData.put(key, data);
    }

    public static Object getData(String key) {
        return internalData.get(key);
    }

    private int executionStrategy(CommandLine.ParseResult parseResult) {
        return new CommandLine.RunLast().execute(parseResult); // default execution strategy
    }
}
