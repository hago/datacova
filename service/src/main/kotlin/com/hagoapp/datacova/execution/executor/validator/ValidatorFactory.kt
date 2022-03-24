/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.executor.validator

import com.hagoapp.datacova.Application
import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.verification.Configuration
import com.hagoapp.datacova.execution.Validator
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.lang.reflect.Constructor

class ValidatorFactory {
    companion object {

        private val validatorMap = mutableMapOf<Int, Constructor<out Validator>>()
        private val logger = CoVaLogger.getLogger()

        init {
            val r = Reflections(Application::class.java.packageName, Scanners.SubTypes)
            r.getSubTypesOf(Validator::class.java).forEach { clz ->
                try {
                    val constructor = clz.getConstructor()
                    val instance = constructor.newInstance()
                    val existed = validatorMap.put(instance.supportedVerificationType, constructor)
                    if (existed == null) {
                        logger.info("${clz.canonicalName} found for validator ${instance.supportedVerificationType}")
                    } else {
                        logger.error("${clz.canonicalName} and ${existed.newInstance()::class.java.canonicalName} conflicts for validator ${instance.supportedVerificationType}")
                    }
                } catch (e: Exception) {
                    logger.error("Validator register failed for ${clz.canonicalName}: ${e.message}")
                }
            }
        }

        fun createValidator(config: Configuration): Validator {
            val clz = validatorMap[config.type] ?: throw CoVaException("Validator for type ${config.type} not found")
            return clz.newInstance()
        }
    }
}