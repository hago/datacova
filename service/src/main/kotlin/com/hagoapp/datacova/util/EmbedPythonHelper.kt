/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util

import org.apache.commons.net.ntp.TimeStamp
import org.python.core.*
import org.python.util.PythonInterpreter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EmbedPythonHelper {
    companion object {

        private val pySystemState = PySystemState()
        private val threadState: ThreadState

        init {
            pySystemState.setdefaultencoding("utf-8")
            threadState = ThreadState(pySystemState)
        }

        @Suppress("UNCHECKED_CAST")
        fun toPyObject(data: Any?): PyObject {
            return when (data) {
                is PyObject -> data
                null -> PyObject(PyNone.TYPE)
                is Int -> PyInteger(data)
                is Long -> PyLong(data)
                is String -> PyUnicode(data)
                is Boolean -> PyBoolean(data)
                is Float -> PyFloat(data)
                is Double -> PyFloat(data)
                is Map<*, *> -> mapToPythonDictionary(data as Map<out Any, Any?>)
                is ZonedDateTime -> PyLong(data.toInstant().toEpochMilli())
                is LocalDateTime -> PyLong(data.toInstant(ZoneOffset.of(ZoneId.systemDefault().id)).toEpochMilli())
                is TimeStamp -> PyLong(data.ntpValue())
                is Set<*> -> setToPythonTuple(data)
                is List<*> -> listToPythonList(data)
                else -> throw UnsupportedOperationException("type ${data::class.java} is not supported to be converted to PyObject")
            }
        }

        @Suppress("UNCHECKED_CAST")
        fun fromPyObject(data: PyObject): Any? {
            return when (data.type) {
                PyNone.TYPE -> null
                PyString.TYPE -> data.asString()
                PyInteger.TYPE -> data.asInt()
                PyLong.TYPE -> data.asLong()
                PyFloat.TYPE -> data.asDouble()
                PyBoolean.TYPE -> data.asInt() > 0
                PyTuple.TYPE -> data.asIterable().map { fromPyObject(it) }.toSet()
                PyList.TYPE -> data.asIterable().map { fromPyObject(it) }.toList()
                PyDictionary.TYPE -> pyDictionaryToMap(data as PyDictionary)
                else -> throw UnsupportedOperationException("python type ${data.type.name} is not supported to be converted to Java type")
            }
        }

        fun runCodeBlock(codeBlock: String, vararg args: Any?): Any? {
            val keywords = listOf<String>().toTypedArray()
            val globals = PyObject(PyNone.TYPE)
            val defaults = listOf<PyObject>().toTypedArray()
            val closure = PyObject(PyNone.TYPE)
            PythonInterpreter().use { py ->
                val code = py.compile(codeBlock)
                val ret = code.call(
                    threadState,
                    args.map { toPyObject(it) }.toTypedArray(),
                    keywords,
                    globals,
                    defaults,
                    closure
                )
                return fromPyObject(ret)
            }
        }

        private fun mapToPythonDictionary(input: Map<out Any, Any?>): PyDictionary {
            val dict = input.map {
                Pair(toPyObject(it.key), toPyObject(it.value))
            }.toMap()
            return PyDictionary(dict)
        }

        private fun pyDictionaryToMap(input: PyDictionary): Map<Any, Any?> {
            return input.map.map { entry ->
                Pair(fromPyObject(entry.key)!!, fromPyObject(entry.value))
            }.toMap()
        }

        private fun listToPythonList(input: List<Any?>): PyList {
            val l = input.map { elem -> toPyObject(elem) }
            return PyList(l)
        }

        private fun setToPythonTuple(input: Set<Any?>): PyTuple {
            val l = input.map { elem -> toPyObject(elem) }
            return PyTuple(*l.toTypedArray())
        }
    }
}
