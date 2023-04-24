/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.surveyor.utils

import org.graalvm.polyglot.Context
import org.graalvm.polyglot.Value
import org.slf4j.LoggerFactory
import java.io.Closeable

class EmbedJsFunctionHelper(private val jsCode: String) : Closeable {

    private val context: Context = Context.newBuilder("js")
        .allowCreateProcess(false)
        .option("js.ecmascript-version", "2020")
        .build()
    private val logger = LoggerFactory.getLogger(EmbedJsFunctionHelper::class.java)

    private val expression: Value = context.eval("js", jsCode)

    init {
        if (!expression.canExecute()) {
            throw UnsupportedOperationException("Not executable: $jsCode")
        }
    }

    fun execute(vararg params: Any?): Any? {
        return try {
            val result = expression.execute(*params)
            logger.debug("result: {}", result)
            fromJsValue(result)
        } catch (e: Throwable) {
            logger.error(
                "Execution of js snippet error: {}, source: {}, params: {}",
                e.message,
                jsCode,
                params.toList()
            )
            throw e
        }
    }

    private fun fromJsValue(value: Value): Any? {
        return when {
            value.isNull -> null
            value.isBoolean -> value.asBoolean()
            value.isNumber -> value.asDouble()
            value.isDate -> value.asDate()
            value.isDuration -> value.asDuration()
            value.isTime -> value.asTime()
            value.isTimeZone -> value.asTimeZone()
            value.isInstant -> value.asInstant()
            value.hasArrayElements() -> (0 until value.arraySize).map { fromJsValue(value.getArrayElement(it)) }
            value.hasHashEntries() -> {
                val m = mutableMapOf<Any, Any?>()
                val ki = value.hashKeysIterator
                while (ki.hasIteratorNextElement()) {
                    val k = ki.iteratorNextElement
                    m[fromJsValue(k)!!] = fromJsValue(value.getHashValue(k))
                }
                m
            }

            value.isIterator -> {
                val l = mutableListOf<Any?>()
                while (value.hasIteratorNextElement()) {
                    val e = value.iteratorNextElement
                    l.add(fromJsValue(e))
                }
            }

            value.hasMembers() -> value.memberKeys.map { fromJsValue(value.getMember(it)) }
            else -> value.asString()
        }
    }

    override fun close() {
        try {
            context.close()
        } catch (e: Exception) {
            logger.warn("Error occurs while closing polyglot context: {}", e.message)
        }
    }
}
