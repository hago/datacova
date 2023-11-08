package com.hagoapp.datacova.dispatcher

import com.hagoapp.datacova.utility.BytesOps
import java.net.Socket

/**
 * Packet parser to pack and unpack message as byte sequence to transmit through socket.
 *
 * @constructor Create empty Packet parser
 */
class PacketParser {
    companion object {

        private const val DEFAULT_TIME_OUT = 500

        @JvmStatic
        fun readPacket(socket: Socket, timeout: Int = DEFAULT_TIME_OUT): ByteArray {
            socket.soTimeout = if (timeout > 0) timeout else DEFAULT_TIME_OUT
            val lenBuffer = socket.getInputStream().readNBytes(4)
            val len = BytesOps.bytesToInt(lenBuffer)
            return socket.getInputStream().readNBytes(len)
        }

        @JvmStatic
        fun writePacket(socket: Socket, data: ByteArray, timeout: Int = DEFAULT_TIME_OUT) {
            socket.soTimeout = if (timeout > 0) timeout else DEFAULT_TIME_OUT
            val lenBytes = BytesOps.intToBytes(data.size)
            socket.getOutputStream().write(lenBytes)
            socket.getOutputStream().write(data)
        }
    }
}
