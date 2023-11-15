/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher.server

import com.hagoapp.datacova.dispatcher.ClientMessageHandler
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
}
