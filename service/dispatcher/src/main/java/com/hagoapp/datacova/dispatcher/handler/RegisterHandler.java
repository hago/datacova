/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher.handler;

import com.hagoapp.datacova.dispatcher.ClientMessageHandler;
import com.hagoapp.datacova.message.RegisterMessage;
import com.hagoapp.datacova.message.RegisterResponseMessage;

import java.net.Socket;
import java.util.UUID;

/**
 * The handler to accept registration from web socket client, which is a worker.
 *
 * @author suncjs
 * @since 0.5
 */
public class RegisterHandler implements ClientMessageHandler.MessageHandler {
    @Override
    public Object handle(Object message) {
        if (message.getClass() != RegisterMessage.class) {
            throw new UnsupportedOperationException("Not a register message!");
        }
        var reg = (RegisterMessage)message;
        var name = reg.getName() != null ? reg.getName() : UUID.randomUUID().toString();
        return new RegisterResponseMessage(true, name);
    }
}