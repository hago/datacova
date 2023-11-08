/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker

import com.google.gson.Gson
import com.hagoapp.datacova.worker.command.Parser
import java.io.File
import java.io.FileInputStream

class Application {
    companion object {

        lateinit var config: Config
            private set
        private var messenger: ServerMessenger? = null

        @JvmStatic
        fun main(args: Array<String>) {
            val configPath = if (args.isNotEmpty()) args[0] else null
            config = loadConfig(configPath)
            starSocketClient()
            while (true) {
                println("input command, ? for help")
                val kin = readln()
                val r = Parser.runCommand(kin)
                if (r.exit) {
                    break
                }
            }
        }

        private const val DEFAULT_CONFIG_PATH = "./worker.conf"
        private fun loadConfig(path: String?): Config {
            val fn = if ((path == null) || !File(path).exists()) {
                val f = File(DEFAULT_CONFIG_PATH)
                if (!f.exists()) {
                    throw UnsupportedOperationException("config file not found, from neither $path nor ${f.absolutePath}.")
                }
                DEFAULT_CONFIG_PATH
            } else {
                path
            }
            return FileInputStream(fn).use {
                Gson().fromJson(String(it.readAllBytes()), Config::class.java)
            }
        }

        private fun starSocketClient() {
            messenger = ServerMessenger.create(config)
        }
    }
}
