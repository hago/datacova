/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker

import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.message.*
import com.hagoapp.datacova.utility.net.SocketPacketParser
import com.hagoapp.datacova.worker.command.Parser
import com.hagoapp.datacova.worker.execution.CachedDbConfigLookup
import com.hagoapp.datacova.worker.execution.DbConfigLoader
import com.hagoapp.f2t.database.config.DbConfigReader
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Server messenger to communicate with server via web socket.
 *
 * @constructor Create empty Server messenger
 */
object ServerMessenger {

    private const val INTERVAL_SECONDS = 5

    private val exitFlag = AtomicBoolean(false)
    private val logger = LoggerFactory.getLogger(ServerMessenger::class.java)
    private lateinit var socket: Socket
    private var name: String? = null
    private val config = Application.oneApp().config

    fun start() {
        startWorker()
        while (true) {
            val input = readln().trim()
            val result = Parser.runCommand(input)
            if (result.exit) {
                break
            }
        }
    }

    private fun startWorker() {
        Thread {
            while (!exitFlag.get()) {
                try {
                    connect()
                } catch (e: IOException) {
                    logger.error("connecting to {}:{} failed: {}", config.server, config.port, e.message)
                    logger.info("sleep {} seconds to reconnect", INTERVAL_SECONDS)
                    Thread.sleep(INTERVAL_SECONDS * 1000L)
                    socket.close()
                    continue
                }
                try {
                    communicate()
                } catch (e: SocketException) {
                    logger.info("socket error: {}, sleep {} seconds to reconnect", e.message, INTERVAL_SECONDS)
                    Thread.sleep(INTERVAL_SECONDS * 1000L)
                    socket.close()
                }
            }
            try {
                socket.close()
            } finally {
                logger.debug("worker {} exit", name)
            }
        }.start()
    }

    private fun connect() {
        socket = Socket()
        socket.keepAlive = true
        socket.connect(InetSocketAddress(config.server, config.port))
        register()
    }

    private fun communicate() {
        while (!exitFlag.get()) {
            val received = SocketPacketParser.readPacket(socket)
            if (received == null) {
                logger.debug("no data, sleep {} seconds", INTERVAL_SECONDS)
                Thread.sleep(INTERVAL_SECONDS * 1000L)
                continue
            }
            val msg = MessageReader.readMessage(received)
            if (msg == null) {
                logger.error("unrecognized data received, ignored")
                continue
            }
            handleMessage(msg)
        }
    }

    private fun register() {
        val message = RegisterMessage(config.group, config.authKey, name)
        val load = MessageWriter.toBytes(message)
        SocketPacketParser.writePacket(socket, load)
        logger.debug("registration sent")
    }

    fun shutDown() {
        exitFlag.set(true)
    }

    private fun handleMessage(message: Any) {
        when (message) {
            is RegisterResponseMessage -> handleRegisterResponseMessage(message)
            is TaskExecutionMessage -> handleTaskExecutionMessage(message)
            else -> {
                logger.error("Unexpected message with type {}, ignored", message::class.java.canonicalName)
            }
        }
    }

    private fun handleRegisterResponseMessage(msg: RegisterResponseMessage) {
        if (msg.acknowledged) {
            name = msg.name
            logger.info("Registration succeeded with name {}", msg.name)
        } else {
            logger.error("Register not acknowledged: {}, registration not successful", msg.name)
        }
    }

    private fun handleTaskExecutionMessage(msg: TaskExecutionMessage) {
        val te = TaskExecution.loadFromJson(msg.taskExecutionJob)
        val connections = msg.connections.map { Pair(
            it.key,
            DbConfigReader.json2DbConfig(it.value)
        ) }.toMap()
        DbConfigLoader.provider = CachedDbConfigLookup(connections)
        Worker(te).execute()
    }
}
