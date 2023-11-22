/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher.server

import com.hagoapp.datacova.dispatcher.ClientMessageHandler
import com.hagoapp.datacova.message.MessageReader
import com.hagoapp.datacova.message.MessageWriter
import com.hagoapp.datacova.message.RegisterMessage
import com.hagoapp.datacova.message.RegisterResponseMessage
import com.hagoapp.datacova.utility.net.SocketPacketParser
import org.slf4j.LoggerFactory
import java.net.Socket
import java.net.SocketException
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Worker speaker, the class to talk with worker client.
 *
 * @property socket the socket connected with worker
 * @constructor Create speaker with worker socket
 */
class WorkerSpeaker(private val socket: Socket) : Runnable {

    val id = UUID.randomUUID().toString()
    val shouldClose = AtomicBoolean(false)
    private val logger = LoggerFactory.getLogger(WorkerSpeaker::class.java)

    override fun run() {
        if (!register()) {
            logger.debug("unsuccessful registration")
            return
        }
        while (!shouldClose.get()) {
            try {
                val data = SocketPacketParser.readPacket(socket)
                if (data == null) {
                    Thread.sleep(500L)
                    continue
                }
                val rsp = ClientMessageHandler.get().handle(data)
                if (rsp != null) {
                    SocketPacketParser.writePacket(socket, rsp)
                }
            } catch (e: SocketException) {
                logger.error("socket error {}", e.message)
                break
            }
        }
        logger.debug("exit client socket thread")
    }

    private fun register(): Boolean {
        val data = SocketPacketParser.readPacket(socket)
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
        SocketPacketParser.writePacket(socket, MessageWriter.toBytes(response))
        return name != null
    }
}
