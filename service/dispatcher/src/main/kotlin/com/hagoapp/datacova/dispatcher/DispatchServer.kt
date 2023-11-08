/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher

import com.hagoapp.datacova.utility.net.SocketPacketParser
import org.slf4j.LoggerFactory
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.atomic.AtomicBoolean

object DispatchServer {

    private val logger = LoggerFactory.getLogger(DispatchServer::class.java)
    private val config = Application.config
    private val shouldClose = AtomicBoolean(false)
    private val speakers = mutableListOf<SocketSpeaker>()

    fun start() {
        ServerSocket(config.port).use {
            logger.info("socket server started")
            while (!shouldClose.get()) {
                val sk = it.accept()
                val speaker = SocketSpeaker(sk)
                speakers.add(speaker)
                Thread(speaker).start()
            }
            logger.info("socket server, closed")
        }
    }

    fun stop() {
        shouldClose.set(true)
    }

    private class SocketSpeaker(private val socket: Socket) : Runnable {

        var shouldClose = AtomicBoolean(false)

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
}
