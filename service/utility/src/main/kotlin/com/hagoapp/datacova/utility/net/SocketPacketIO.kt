/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.net

import com.hagoapp.datacova.utility.BytesOps
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * Packet parser to pack and unpack message as byte sequence to transmit through socket.
 *
 * @constructor Create empty Packet parser
 */
object SocketPacketIO {

    private const val DEFAULT_TIME_OUT = 500

    fun readPacket(socket: Socket, timeout: Int = DEFAULT_TIME_OUT): ByteArray? {
        return try {
            socket.soTimeout = if (timeout > 0) timeout else DEFAULT_TIME_OUT
            val lenBuffer = socket.getInputStream().readNBytes(4)
            val len = BytesOps.bytesToInt(lenBuffer)
            socket.getInputStream().readNBytes(len)
        } catch (e: SocketTimeoutException) {
            null
        }
    }

    fun writePacket(socket: Socket, data: ByteArray, timeout: Int = DEFAULT_TIME_OUT) {
        socket.soTimeout = if (timeout > 0) timeout else DEFAULT_TIME_OUT
        val lenBytes = BytesOps.intToBytes(data.size)
        socket.getOutputStream().write(lenBytes)
        socket.getOutputStream().write(data)
    }
}
