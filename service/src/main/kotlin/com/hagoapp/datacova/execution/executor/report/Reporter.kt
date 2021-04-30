/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.executor.report

import com.hagoapp.datacova.Application
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.entity.execution.ExecutionDetail
import com.hagoapp.datacova.entity.execution.TaskExecution
import com.hagoapp.datacova.util.StackTraceWriter
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.lang.reflect.Constructor

class Reporter {
    companion object {

        private val reporterMap = mutableListOf<Constructor<out ExecutionReport>>()
        private val logger = CoVaLogger.getLogger()

        init {
            Reflections(Application::class.java.packageName, SubTypesScanner())
                .getSubTypesOf(ExecutionReport::class.java).forEach { clz ->
                    reporterMap.add(clz.getConstructor())
                }
        }

        @JvmStatic
        fun sendReport(execution: TaskExecution, detail: ExecutionDetail) {
            reporterMap.forEach {
                val instance = it.newInstance()
                val t = Thread(Runnable {
                    try {
                        instance.sendReport(execution, detail)
                    } catch (e: Throwable) {
                        logger.error("Execution Reporter {} error: {}", instance::class.java.canonicalName, e.message)
                        StackTraceWriter.write(e, logger)
                    }
                })
                t.isDaemon = true
                t.start()
            }
        }
    }
}
