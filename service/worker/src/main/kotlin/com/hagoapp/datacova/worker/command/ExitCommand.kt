/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.command

import com.hagoapp.datacova.worker.Application
import com.hagoapp.datacova.worker.ServerMessenger

/**
 * Command to exit.
 *
 * @author suncjs
 * @since 0.5
 */
@CommandName(names = ["exit", "quit"], help = "exit application")
class ExitCommand : Command {
    override fun run(cmd: String, args: List<String>): Result {
        ServerMessenger.create(Application.config).shutDown()
        return Result(true)
    }
}
