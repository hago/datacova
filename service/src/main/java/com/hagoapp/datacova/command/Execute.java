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

@CommandLine.Command(name = "exec", description = "execute a task")
public class Execute extends CommandWithConfig {

    @CommandLine.Option(names = {"-i", "--id"}, description = "id of task to be executed", required = true)
    private long taskId;

    @Override
    public Integer call() throws CoVaException {
        CoVaConfig.loadConfig(configFile);

        return super.call();
    }
}
