/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.executor.validator

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.entity.action.verification.conf.EmbedPythonConfig
import com.hagoapp.datacova.execution.Validator
import com.hagoapp.datacova.util.EmbedPythonHelper
import com.hagoapp.f2t.DataRow
import org.python.core.*
import org.python.util.PythonInterpreter

class EmbedPythonValidator : Validator() {

    companion object {
        private val ps = PySystemState()

        init {
            ps.setdefaultencoding("utf-8")
        }
    }

    private lateinit var conf: EmbedPythonConfig
    private val py = PythonInterpreter()
    private lateinit var code: PyCode
    private val pyDefaults = listOf<PyObject>()
    private val pyGlobals: PyObject? = null
    private val pyClosure: PyObject? = null
    private val threadState = ThreadState(ps)
    private val logger = CoVaLogger.getLogger()

    override fun prepare() {
        if (config !is EmbedPythonConfig) {
            throw CoVaException("Not a valid embed python config")
        }
        code = py.compile(conf.snippet, "snippet")
    }

    override fun getSupportedVerificationType(): Int {
        return EmbedPythonConfig.EMBED_Python_CONFIGURATION_TYPE
    }

    private fun check(paramMap: Map<PyObject, PyObject?>): Boolean {
        val rowDict = PyDictionary(paramMap)
        val result = code.call(threadState, rowDict, pyGlobals, pyDefaults.toTypedArray(), pyClosure)
        val ret = EmbedPythonHelper.fromPyObject(result)
        return if (ret is Boolean) ret else false
    }

    override fun verify(row: DataRow): Map<String, Any?> {
        val loaded = fieldLoader.loadField(row)
        val dict = loaded.map {
            Pair(EmbedPythonHelper.toPyObject(it.key), EmbedPythonHelper.toPyObject(it.value.data))
        }.toMap()
        return if (check(dict)) mapOf() else
            loaded.map { Pair(it.key, it.value.data) }.toMap()
    }

    override fun close() {
        try {
            py.close()
        } finally {
            super.close()
        }
    }
}
