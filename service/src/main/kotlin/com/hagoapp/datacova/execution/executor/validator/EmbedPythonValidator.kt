/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.executor.validator

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.verification.conf.EmbedPythonConfig
import com.hagoapp.datacova.execution.Validator
import com.hagoapp.datacova.util.EmbedPythonHelper
import com.hagoapp.f2t.DataRow
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets

class EmbedPythonValidator : Validator() {

    companion object {
        const val ROW_VARIABLE = "row"
        const val RESULT_VARIABLE = "result"

        private val logger = LoggerFactory.getLogger(EmbedPythonHelper::class.java)
    }

    private lateinit var conf: EmbedPythonConfig
    private val py = EmbedPythonHelper()

    init {
        py.sourceEncode = StandardCharsets.UTF_8
    }

    override fun prepare() {
        if (config !is EmbedPythonConfig) {
            throw CoVaException("Not a valid embed python config")
        }
        conf = config as EmbedPythonConfig
    }

    override fun getSupportedVerificationType(): Int {
        return EmbedPythonConfig.EMBED_Python_CONFIGURATION_TYPE
    }

    override fun verify(row: DataRow): Map<String, Any?> {
        val rowLine = fieldLoader.loadField(row).map { Pair(it.key, it.value.data) }.toMap()
        val out = py.execCodeBlock(
            conf.snippet,
            mapOf(ROW_VARIABLE to rowLine),
            setOf(RESULT_VARIABLE)
        )
        logger.debug("eval: {} return {}", rowLine, out)
        val ret = if (out[RESULT_VARIABLE] == null) false else out[RESULT_VARIABLE] as Boolean
        return if (ret) mapOf() else rowLine
    }

    override fun close() {
        try {
            py.close()
        } finally {
            super.close()
        }
    }
}
