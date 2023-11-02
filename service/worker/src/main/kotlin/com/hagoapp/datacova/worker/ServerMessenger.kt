/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.worker

import com.hagoapp.datacova.message.MessageReader
import com.hagoapp.datacova.message.MessageWriter
import com.hagoapp.datacova.message.RegisterMessage
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Server messenger to communicate with server via web socket.
 *
 * @constructor Create empty Server messenger
 */
class ServerMessenger private constructor(private val config: Config) : WebSocket.Listener {

    companion object {
        private const val INTERVAL_SECONDS = 10
        private lateinit var messenger: ServerMessenger

        fun create(config: Config): ServerMessenger {
            if (!this::messenger.isInitialized) {
                messenger = ServerMessenger(config)
            }
            return messenger
        }
    }

    private val exitFlag = AtomicBoolean(false)

    private val logger = LoggerFactory.getLogger(ServerMessenger::class.java)

    private var sk: WebSocket? = null

    init {
        Thread { tryConnect() }.start()
    }

    override fun onOpen(webSocket: WebSocket) {
        super.onOpen(webSocket)
        logger.debug("web socket connected")
        val message = RegisterMessage(config.group)
        val load = MessageWriter.toBytes(message)
        webSocket.sendBinary(ByteBuffer.wrap(load), true)
    }

    private var reader: MessageReader? = null

    override fun onText(webSocket: WebSocket?, data: CharSequence?, last: Boolean): CompletionStage<*> {
        logger.warn("Unexpected text message: {} {}", data, last)
        return super.onText(webSocket, data, last)
    }

    override fun onBinary(webSocket: WebSocket, data: ByteBuffer, last: Boolean): CompletionStage<*> {
        if (reader == null) {
            reader = MessageReader()
        }
        reader!!.update(data.array())
        if (last) {
            val message = reader!!.parseMessage()
            reader!!.close()
            reader = null
            handleMessage(message)
        }
        return super.onBinary(webSocket, data, last)
    }

    override fun onPing(webSocket: WebSocket?, message: ByteBuffer?): CompletionStage<*> {
        logger.warn("Unexpected ping message: {}", message)
        return super.onPing(webSocket, message)
    }

    override fun onPong(webSocket: WebSocket?, message: ByteBuffer?): CompletionStage<*> {
        logger.warn("Unexpected pong message: {}", message)
        return super.onPong(webSocket, message)
    }

    override fun onClose(webSocket: WebSocket?, statusCode: Int, reason: String?): CompletionStage<*> {
        return super.onClose(webSocket, statusCode, reason)
    }

    override fun onError(webSocket: WebSocket, error: Throwable) {
        super.onError(webSocket, error)
        logger.error("Web socket error: {}", error.message)
        if (webSocket.isInputClosed || webSocket.isOutputClosed) {
            logger.warn("Web socket disconnected, reconnecting...")
            sk = null
            tryConnect()
        }
    }

    private fun connect(): WebSocket {
        return HttpClient.newHttpClient().newWebSocketBuilder()
            .buildAsync(URI.create(config.server), this)
            .join()
    }

    private fun tryConnect() {
        while (!exitFlag.get()) {
            try {
                logger.info("trying to connect {}", config.server)
                sk = connect()
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
                logger.warn("interrupt web socket thread: {}", Thread.interrupted())
            } catch (ex: CompletionException) {
                logger.error("reconnect fails: {}, wait for {} seconds", ex.message, INTERVAL_SECONDS)
                Thread.sleep(1000L * INTERVAL_SECONDS)
            }
        }
    }

    fun shutDown() {
        exitFlag.set(true)
        if ((sk != null) && !sk!!.isOutputClosed) {
            sk!!.sendBinary(null, true)
        }
    }

    private fun handleMessage(message: Any?) {
        TODO()
    }
}
