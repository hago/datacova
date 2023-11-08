package com.hagoapp.datacova.dispatcher

import com.hagoapp.datacova.utility.BytesOps
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * Packet parser to pack and unpack message as byte sequence to transmit through socket.
 *
 * @constructor Create empty Packet parser
 */
object SocketPacketParser {

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
