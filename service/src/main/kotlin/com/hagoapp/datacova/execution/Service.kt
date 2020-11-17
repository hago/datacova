/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.config.CoVaConfig
import java.util.concurrent.locks.ReentrantReadWriteLock

class Service private constructor() {

    companion object {

        private const val EXECUTION_QUERY_INTERVAL = 500L
        private const val LOADING_TASk_EXECUTION_BATCH = 10
        private const val SERVICE_STOP_MAX_ATTEMPT = 10
        private var executor: Service? = null

        @Synchronized
        fun getExecutor(): Service {
            if (executor == null) {
                executor = Service()
            }
            return executor!!
        }
    }

    private val logger = CoVaLogger.getLogger()
    private val config = CoVaConfig.getConfig()
    private var exitFlag = false
    private var serviceStopped = true
    private var workerCount: Int = 0
    private var counterLock = ReentrantReadWriteLock()

    fun startExecutionService() {
        logger.info("Start Execution Service")
        startExecutionWatcher()
        logger.info("Execution Service started")
    }

    fun stopExecutionService() {
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

    private fun startExecutionWatcher() {
//
    }

}
