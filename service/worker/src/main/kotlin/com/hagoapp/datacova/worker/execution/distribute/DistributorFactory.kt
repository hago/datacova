/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker.execution.distribute

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.LibCova
import com.hagoapp.datacova.worker.distribute.Distributor
import com.hagoapp.datacova.worker.distribute.TaskActionDistribute
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.slf4j.LoggerFactory
import java.lang.reflect.Constructor

class DistributorFactory {
    companion object {

        private val logger = LoggerFactory.getLogger(DistributorFactory::class.java)

        private val distributorMap = mutableMapOf<String, Constructor<out Distributor>>()

        init {
            val r = Reflections(LibCova.DEFAULT_NAMESPACE, Scanners.SubTypes)
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

        fun getDistributor(action: TaskActionDistribute): Distributor {
            val conf = action.configuration
            val constructor =
                distributorMap[conf.type] ?: throw CoVaException("distributor type ${conf.type} not supported")
            val instance = constructor.newInstance()
            instance.init(action)
            return instance
        }
    }
}
