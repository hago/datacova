/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.cli;

import com.hagoapp.datacova.worker.ServerMessenger;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "worker")
public class WorkerCmd implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        ServerMessenger.INSTANCE.start();
        return 0;
    }
}
