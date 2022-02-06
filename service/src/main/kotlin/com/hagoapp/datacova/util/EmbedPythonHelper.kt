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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.io.InputStream
import java.lang.NullPointerException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class EmbedPythonHelper : Closeable {
    companion object {

        private val pySystemState = PySystemState()
        private val threadState: ThreadState
        private const val PEP_263_TEMPLATE = "# -*- coding: %s -*-"
        private val logger: Logger = LoggerFactory.getLogger(EmbedPythonHelper::class.java)

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
                PyString.TYPE, PyUnicode.TYPE -> data.asString()
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

        fun execCodeBlockOnce(
            code: String,
            presetVariables: Map<String, Any?>,
            outVariables: Set<String>,
            allowImportModules: Set<String> = setOf(),
            sourceEncoding: Charset = StandardCharsets.UTF_8
        ): Map<String, Any?> {
            EmbedPythonHelper().use {
                it.sourceEncode = sourceEncoding
                it.allowedImportModules = allowImportModules
                return it.execCodeBlock(code, presetVariables, outVariables)
            }
        }

        private fun exec(interpreter: PythonInterpreter, code: String, encoding: Charset = StandardCharsets.UTF_8) {
            logger.debug("python execute: {}", code)
            ByteArrayInputStream(code.toByteArray(encoding)).use { stream ->
                interpreter.execfile(stream)
            }
        }

        private val importRegex = Regex.fromLiteral("\\s*import\\s+(\\S+?)")
        private fun isImportClauseShouldSkip(line: String, allowedImportModules: Set<String>): Boolean {
            val m = importRegex.find(line) ?: return false
            return allowedImportModules.contains(m.value)
        }
    }

    private val interpreter = PythonInterpreter()
    var sourceEncode: Charset = StandardCharsets.UTF_8
    var allowedImportModules: Set<String> = setOf()

    fun execCodeBlock(
        code: String,
        presetVariables: Map<String, Any?>,
        outVariables: Set<String>
    ): Map<String, Any?> {
        interpreter.cleanup()
        val pep263 = String.format(PEP_263_TEMPLATE, sourceEncode.name())
        interpreter.exec(pep263)
        presetVariables.forEach { (name, value) ->
            interpreter.set(name, toPyObject(value))
        }
        val lines = code.split("\r\n").filter { it.isNotBlank() }
        for (line in lines) {
            if (isImportClauseShouldSkip(line, allowedImportModules)) {
                continue
            }
            exec(interpreter, line, sourceEncode)
        }
        return outVariables.associateWith { name ->
            try {
                val po = interpreter.get(name)
                fromPyObject(po)
            } catch (e: NullPointerException) {
                null
            }
        }.filter { it.value != null }
    }

    override fun close() {
        try {
            interpreter.close()
        } catch (t: Throwable) {
            //
        }
    }
}
