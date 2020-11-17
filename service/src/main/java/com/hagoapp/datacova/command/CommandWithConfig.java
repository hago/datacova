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
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;

@CommandLine.Command
public class CommandWithConfig implements Callable<Integer> {
    @CommandLine.Option(names = {"-c", "--config"}, required = true)
    protected String configFile;

    @Override
    public Integer call() throws CoVaException {
        if ((this.configFile == null) || this.configFile.isBlank()) {
            throw new CoVaException("config file not provided");
        }
        if (!new File(configFile).exists()) {
            throw new CoVaException(String.format("config file %s not found", configFile));
        }
        CoVaConfig.loadConfig(configFile);
        return 0;
    }
}
