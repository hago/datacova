/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.distribute

import com.hagoapp.datacova.Application
import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.distribute.Distributor
import com.hagoapp.datacova.entity.action.distribute.Configuration
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import java.lang.reflect.Constructor

class DistributorFactory {
    companion object {

        private val logger = CoVaLogger.getLogger()

        private val distributorMap = mutableMapOf<Int, Constructor<out Distributor>>()

        init {
            val r = Reflections(Application::class.java.packageName, SubTypesScanner())
            r.getSubTypesOf(Distributor::class.java).forEach { clz ->
                val constructor = clz.getConstructor()
                val instance = constructor.newInstance()
                val t = instance.supportedDistributionType()
                if (distributorMap.containsKey(t)) {
                    logger.error(
                        "distributor type {} from {} conflicts with{}",
                        t, clz.canonicalName, distributorMap[t]!!.declaringClass.canonicalName
                    )
                } else {
                    distributorMap[t] = constructor
                }
            }
        }

        fun getDistributor(conf: Configuration): Distributor {
            val constructor =
                distributorMap[conf.type] ?: throw CoVaException("distributor type ${conf.type} not supported")
            return constructor.newInstance()
        }
    }
}
