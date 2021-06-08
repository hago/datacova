/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.executor

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.dispatcher.DispatcherInvoker

class Executor private constructor() {
    companion object {
        private val executor = Executor()
        private const val SERVICE_STOP_MAX_ATTEMPT = 10

        fun getExecutor(): Executor {
            return executor
        }
    }

    private val config = CoVaConfig.getConfig().executor
    private val logger = CoVaLogger.getLogger()
    private var exitFlag = false
    private var serviceStopped = true

    fun start() {
        if (!DispatcherInvoker(config).register()) {
            return
        }
    }

    fun stop() {
        logger.info("Stop Execution Service")
        exitFlag = true
        for (i in 0 until SERVICE_STOP_MAX_ATTEMPT) {
            if (serviceStopped) {
                break
            }
            System.err.println("waiting for Execution Service to stop")
            Thread.sleep(2000)
        }
        logger.info("Execution Service Stopped")
    }
}
