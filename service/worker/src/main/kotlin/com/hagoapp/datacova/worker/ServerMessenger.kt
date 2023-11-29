/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker

import com.hagoapp.datacova.lib.execution.ExecutionDetail
import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.message.*
import com.hagoapp.datacova.utility.net.SocketPacketIO
import com.hagoapp.datacova.worker.command.Parser
import com.hagoapp.datacova.worker.execution.CachedDbConfigLookup
import com.hagoapp.datacova.worker.execution.DbConfigLoader
import com.hagoapp.datacova.worker.execution.TaskExecutionWatcher
import com.hagoapp.f2t.database.config.DbConfigReader
import org.slf4j.LoggerFactory
import java.io.IOException
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Server messenger to communicate with server via web socket.
 *
 * @constructor Create empty Server messenger
 */
object ServerMessenger : TaskExecutionWatcher {

    private const val INTERVAL_SECONDS = 5

    private val exitFlag = AtomicBoolean(false)
    private val logger = LoggerFactory.getLogger(ServerMessenger::class.java)
    private lateinit var socket: Socket
    private var name: String? = null
    private val config = Application.oneApp().config
    private var taskExecution: TaskExecution? = null
    private var taskExecutionTime: Long? = null

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
            val received = SocketPacketIO.readPacket(socket)
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
            val rsp = handleMessage(msg)
            if (rsp != null) {
                val bytes = MessageWriter.toBytes(rsp)
                SocketPacketIO.writePacket(socket, bytes)
            }
        }
    }

    private fun register() {
        val message = RegisterMessage(config.group, config.authKey, name, taskExecution?.toJson(), taskExecutionTime)
        val load = MessageWriter.toBytes(message)
        SocketPacketIO.writePacket(socket, load)
        logger.debug("registration sent")
    }

    fun shutDown() {
        exitFlag.set(true)
    }

    private fun handleMessage(message: Any): Any? {
        return when (message) {
            is RegisterResponseMessage -> handleRegisterResponseMessage(message)
            is TaskExecutionMessage -> handleTaskExecutionMessage(message)
            is HeartBeatMessage -> handleHeartbeatMessage(message)
            else -> {
                logger.error("Unexpected message with type {}, ignored", message::class.java.canonicalName)
                null
            }
        }
    }

    private fun handleHeartbeatMessage(msg: HeartBeatMessage): Any {
        val rsp = HeartBeatResponseMessage(Instant.now().toEpochMilli(), msg.id)
        logger.debug("Heartbeat {} responded: {} -> {}", msg.id, msg.timeStamp, rsp.timeStamp)
        return rsp
    }

    private fun handleRegisterResponseMessage(msg: RegisterResponseMessage): Any? {
        if (msg.acknowledged) {
            name = msg.name
            logger.info("Registration succeeded with name {}", msg.name)
        } else {
            logger.error("Register not acknowledged: {}, registration not successful", msg.name)
        }
        return null
    }

    private fun handleTaskExecutionMessage(msg: TaskExecutionMessage): Any? {
        taskExecution = TaskExecution.loadFromJson(msg.taskExecutionJob)
        taskExecutionTime = Instant.now().toEpochMilli()
        val connections = msg.connections.map {
            Pair(
                it.key,
                DbConfigReader.json2DbConfig(it.value)
            )
        }.toMap()
        DbConfigLoader.provider = CachedDbConfigLookup(connections)
        Worker(taskExecution!!).addWatcher(this).execute()
        return null
    }

    override fun onComplete(te: TaskExecution, result: ExecutionDetail) {
        val message = WorkerDoneMessage(te.toJson(), result.toJson())
        val bytes = MessageWriter.toBytes(message)
        SocketPacketIO.writePacket(socket, bytes)
        logger.debug("done message for {} sent", te.id)
        taskExecution = null
        taskExecutionTime = null
        logger.debug("local job info cleaned")
    }

    override fun onError(te: TaskExecution, error: Exception) {
        logger.error("ERROR in execution: {}", error.message)
    }
}
