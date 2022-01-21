/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util

import org.python.core.*
import org.python.util.PythonInterpreter

class EmbedPythonHelper {
    companion object {

        private val pySystemState = PySystemState()
        private val threadState: ThreadState

        init {
            pySystemState.setdefaultencoding("utf-8")
            threadState = ThreadState(pySystemState)
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun toPyObject(data: Any?): PyObject {
            return when (data) {
                is PyObject -> data
                null -> PyObject(PyNone.TYPE)
                is Int -> PyInteger(data)
                is Long -> PyLong(data)
                is String -> PyString(data)
                is Boolean -> PyBoolean(data)
                is Float -> PyFloat(data)
                is Double -> PyFloat(data)
                is Map<*, *> -> mapToPythonDictionary(data as Map<out Any, Any?>)
                else -> throw UnsupportedOperationException("type ${data::class.java} is not supported to be converted to PyObject")
            }
        }

        @JvmStatic
        fun fromPyObject(data: PyObject): Any? {
            return when (data.type) {
                PyNone.TYPE -> null
                PyString.TYPE -> data.asString()
                PyInteger.TYPE -> data.asInt()
                PyLong.TYPE -> data.asLong()
                PyFloat.TYPE -> data.asDouble()
                PyBoolean.TYPE -> data.asInt() > 0
                else -> throw UnsupportedOperationException("python type ${data.type.name} is not supported to be converted to Java type")
            }
        }

        @JvmStatic
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
    }
}
