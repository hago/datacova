/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.command;

import com.hagoapp.datacova.CoVaException;
import picocli.CommandLine;

@CommandLine.Command(name = "stop", description = "stop service")
public class Stop extends CommandWithConfig {
    @Override
    public Integer call() throws CoVaException {
        return super.call();
    }
}
