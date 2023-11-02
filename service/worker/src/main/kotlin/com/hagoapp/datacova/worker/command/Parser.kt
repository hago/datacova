/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.command

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.slf4j.LoggerFactory

/**
 * Command parser.
 *
 * @author suncjs
 * @since 0.5
 */
class Parser {
    companion object {

        val commandMap = mutableMapOf<String, Class<out Command>>()
        private val logger = LoggerFactory.getLogger(Parser::class.java)

        init {
            Reflections("com.hagoapp.datacova.worker.command", Scanners.SubTypes)
                .getSubTypesOf(Command::class.java).forEach { clz ->
                    val anno = clz.getAnnotation(CommandName::class.java)
                    if (anno != null) {
                        anno.names.forEach {
                            val name = it.lowercase()
                            if (commandMap.containsKey(name)) {
                                logger.error(
                                    "CommandName annotation {} conflict: {} -- {}, skipped",
                                    name,
                                    commandMap.getValue(name).canonicalName,
                                    clz.canonicalName
                                )
                            } else {
                                commandMap[name.lowercase()] = clz
                            }
                        }
                    } else {
                        logger.warn("{} doesn't have CommandName annotation, skipped", clz.canonicalName)
                    }
                }
        }

        fun runCommand(input: String): Result {
            val args = input.split(' ').map { it.trim() }
            val cmd = args[0]
            val params = args.takeLast(args.size - 1)
            val clz = commandMap[cmd]
            return if (clz != null) {
                val obj = clz.getConstructor().newInstance()
                obj.run(cmd, params)
            } else {
                UnMatchedCommand().run(cmd, params)
            }
        }
    }
}
