package com.hagoapp.datacova.dispatcher

import java.net.Socket

class SocketSpeaker(private val socket: Socket) : Runnable {

    private val input = socket.getInputStream()
    private val output = socket.getOutputStream()

    override fun run() {
        TODO("Not yet implemented")
    }
}
