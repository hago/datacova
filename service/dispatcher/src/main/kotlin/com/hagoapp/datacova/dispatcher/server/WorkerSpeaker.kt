/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher.server

import com.hagoapp.datacova.dispatcher.ClientMessageHandler
import com.hagoapp.datacova.message.*
import com.hagoapp.datacova.utility.net.SocketPacketIO
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.Socket
import java.net.SocketException
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Worker speaker, the class to talk with worker client.
 *
 * @property socket the socket connected with worker
 * @constructor Create speaker with worker socket
 */
class WorkerSpeaker(private val socket: Socket) : Runnable {

    companion object {
        private const val HEARTBEAT_INTERVAL = 1000L * 60 * 5
        private const val HEARTBEAT_NO_RESPONSE_THRESHOLD = 1000L * 60 * 15
    }

    val id = UUID.randomUUID().toString()
    val shouldClose = AtomicBoolean(false)
    private val logger = LoggerFactory.getLogger(WorkerSpeaker::class.java)
    private var timer: Timer? = null
    private var lastHeartbeatResponseTime = 0L

    override fun run() {
        if (!register()) {
            logger.debug("unsuccessful registration")
            return
        }
        while (!shouldClose.get()) {
            try {
                val data = SocketPacketIO.readPacket(socket)
                if (data == null) {
                    Thread.sleep(500L)
                    continue
                }
                val rsp = ClientMessageHandler.get().handle(this, data)
                if (rsp != null) {
                    SocketPacketIO.writePacket(socket, rsp)
                }
            } catch (e: SocketException) {
                logger.error("socket error {}", e.message)
                break
            }
        }
        timer?.cancel()
        if (socket.isConnected) {
            try {
                socket.close()
            } catch (e: IOException) {
                logger.warn("close socket error: {}", e.message)
            }
        }
        logger.debug("exit client socket thread")
    }

    private fun register(): Boolean {
        val data = SocketPacketIO.readPacket(socket)
        if (data == null) {
            logger.error("1st message after connected should be register, somehow null")
            return false
        }
        val msg = MessageReader.readMessage(data)
        if (msg !is RegisterMessage) {
            logger.error("1st message after connected should be register, somehow it's {}", msg)
            return false
        }
        val name = ServerState.workerRegister(this, msg)
        val response = RegisterResponseMessage(name != null, name ?: "Failed")
        SocketPacketIO.writePacket(socket, MessageWriter.toBytes(response))
        if (name != null) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    val heartbeatTime = Instant.now().toEpochMilli()
                    if ((heartbeatTime - lastHeartbeatResponseTime > HEARTBEAT_NO_RESPONSE_THRESHOLD)
                        && (lastHeartbeatResponseTime > 0)
                    ) {
                        shouldClose.set(true)
                        return
                    }
                    val heartBeatMessage = HeartBeatMessage(heartbeatTime, UUID.randomUUID().toString())
                    sendMessage(heartBeatMessage)
                    logger.debug("heart beat {}", heartBeatMessage.id)
                }
            }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL)
        }
        return name != null
    }

    fun sendMessage(message: Any) {
        val bytes = MessageWriter.toBytes(message)
        SocketPacketIO.writePacket(socket, bytes)
    }

    fun heartbeatResponded(msg: HeartBeatResponseMessage) {
        lastHeartbeatResponseTime = msg.timeStamp
        logger.debug("heart beat responded: {} {}", msg.originId, msg.timeStamp)
    }
}
