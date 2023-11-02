/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.command

/**
 * Command to print help.
 *
 * @author suncjs
 * @since 0.5
 */
@CommandName(names = ["?", "help"], help = "print help message")
class HelpCommand : Command {
    companion object {
        private val helpMap = mutableMapOf<String, String>()
    }

    override fun run(cmd: String, args: List<String>): Result {
        if (helpMap.isEmpty()) {
            init()
        }
        helpMap.forEach { (t, u) ->
            println("[$t] -> $u")
        }
        return Result(false)
    }

    private fun init() {
        val excludes = listOf(UnMatchedCommand::class.java)
        helpMap.putAll(Parser.commandMap.values.toSet()
            .filter { !excludes.contains(it) }
            .associate {
                val anno = it.getAnnotation(CommandName::class.java)
                Pair(anno.names.joinToString(", "), anno.help)
            })
    }
}
