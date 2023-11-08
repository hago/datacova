/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher

import com.google.gson.Gson
import com.hagoapp.datacova.lib.data.TaskExecutionData
import com.hagoapp.datacova.lib.execution.TaskExecution
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

class Application {
    companion object {

        lateinit var config: Config
            private set
            @JvmStatic
            get
        private var exitFlag = AtomicBoolean(false)
        private val logger = LoggerFactory.getLogger(Application::class.java)
        private const val DEFAULT_CONFIG_FILE = "conf.json"

        @JvmStatic
        fun main(args: Array<String>) {
            val configFile = if (args.isNotEmpty()) args[0] else DEFAULT_CONFIG_FILE
            if (!File(configFile).exists()) {
                logger.error("config file not found from {}, exit", configFile)
                return
            }
            config = FileInputStream(configFile).use {
                val json = String(it.readAllBytes(), StandardCharsets.UTF_8)
                Gson().fromJson(json, Config::class.java)
            }
            Thread { DispatchServer.start() }.start()
            while (!exitFlag.get()) {
                /*val l = loadActiveTaskExecution()
                if (l.isEmpty()) {
                    Thread.sleep(5000)
                    continue
                }*/
            }
            logger.info("exit")
        }

        private fun loadActiveTaskExecution(): List<TaskExecution> {
            return TaskExecutionData(config.db).use {
                it.loadQueueingTaskExecution()
            }
        }
    }
}
