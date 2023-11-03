/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution

import com.hagoapp.datacova.Application
import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.execution.executor.BaseTaskActionExecutor
import com.hagoapp.datacova.lib.action.TaskAction
import com.hagoapp.datacova.lib.action.TaskActionFactory
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.slf4j.LoggerFactory
import java.lang.reflect.Constructor

class TaskActionExecutorFactory {
    companion object {

        private val actionExecutorMap = mutableMapOf<Int, Constructor<out BaseTaskActionExecutor>>()
        private val logger = LoggerFactory.getLogger(TaskActionFactory::class.java)

        init {
            val r = Reflections(Application::class.java.packageName, Scanners.SubTypes)
            r.getSubTypesOf(BaseTaskActionExecutor::class.java).forEach { executor ->
                try {
                    val creator = executor.getConstructor()
                    val type = creator.newInstance().getActionType()
                    if (actionExecutorMap.containsKey(type)) {
                        logger.error(
                            "duplicate action type $type between {} and {}",
                            actionExecutorMap.getValue(type).declaringClass.canonicalName, executor.canonicalName
                        )
                    } else {
                        actionExecutorMap[type] = creator
                        logger.info("Action executor for {} registered", type)
                    }
                } catch (e: Exception) {
                    logger.error("Action executor {}} register failed", executor.canonicalName)
                }
            }
        }

        fun createTaskActionExecutor(action: TaskAction): BaseTaskActionExecutor {
            return when (val creator = actionExecutorMap[action.type]) {
                null -> throw CoVaException("executor for action ${action.type} not implemented")
                else -> creator.newInstance()
            }
        }
    }
}
