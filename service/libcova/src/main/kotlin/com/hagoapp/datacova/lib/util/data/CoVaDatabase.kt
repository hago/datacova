/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util.data

import com.hagoapp.datacova.lib.data.DatabaseConfig
import com.hagoapp.datacova.utility.CoVaException
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

abstract class CoVaDatabase(connectionConfig: DatabaseConfig) : Closeable {

    companion object {
        private const val PGSQL_DRIVER_CLASS_NAME = "org.postgresql.Driver"

        init {
            Class.forName(PGSQL_DRIVER_CLASS_NAME)
        }
    }

    private val config = connectionConfig

    protected val connection: Connection

    private val logger = LoggerFactory.getLogger(CoVaDatabase::class.java)

    init {
        if (config.databaseName.isNullOrBlank()) {
            config.databaseName = "postgres"
        }
        if (listOf(config.host, config.username, config.password).any { it == null }) {
            throw CoVaException("Configuration is incomplete")
        }
        val conStr = "jdbc:postgresql://${config.host}:${config.port}/${config.databaseName}"
        val props = Properties()
        props.putAll(mapOf("user" to config.username, "password" to config.password))
        connection = DriverManager.getConnection(conStr, props)
    }

    override fun close() {
        try {
            connection.close()
        } catch (e: Throwable) {
            //
        }
    }
}