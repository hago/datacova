/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher

import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.net.ServerSocket
import java.net.Socket

class DispatchServer private constructor() {
    companion object {
        private val server: DispatchServer = DispatchServer()

        fun getServer(): DispatchServer = server
    }

    private val logger = LoggerFactory.getLogger(DispatchServer::class.java)

    private val config = Application.config

    fun start() {
        ServerSocket(config.port).use {
            val sk = it.accept()
            Thread { dispatch(sk) }.start()
        }
    }

    private fun dispatch(socket: Socket) {
        try {
            val buffer = ByteArray(1024 * 1024)
            val input = ByteArrayOutputStream().use {
                while (true) {
                    val i = socket.getInputStream().read(buffer, 0, buffer.size)
                    if (i < 0) {
                        break
                    }
                    it.write(buffer, 0, i)
                }
                it.toByteArray()
            }
            val msg = ClientMessageHandler.get().handle(input) ?: return
            socket.getOutputStream().write(msg)
        } catch (e: Exception) {
            logger.error("error: {}", e.message)
        } finally {
            try {
                socket.close()
            } catch (ignore: Exception) {
                //
            }
        }
    }
}
