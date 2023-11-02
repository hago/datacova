/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.command

/**
 * Command for unrecognized input.
 *
 * @author suncjs
 * @since 0.5
 */
@CommandName(names = [""])
class UnMatchedCommand : Command {
    override fun run(cmd: String, args: List<String>): Result {
        println("$cmd is not supported, use ? for help")
        return Result(
            false
        )
    }
}