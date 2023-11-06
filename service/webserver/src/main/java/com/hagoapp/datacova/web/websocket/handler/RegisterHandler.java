/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket.handler;

import com.hagoapp.datacova.message.RegisterMessage;
import com.hagoapp.datacova.message.RegisterResponseMessage;
import com.hagoapp.datacova.web.websocket.ClientMessageHandler;
import io.vertx.core.http.ServerWebSocket;

import java.util.UUID;

/**
 * The handler to accept registration from web socket client, which is a worker.
 *
 * @author suncjs
 * @since 0.5
 */
public class RegisterHandler implements ClientMessageHandler.MessageHandler {
    @Override
    public Object handle(Object message, ServerWebSocket serverWebSocket) {
        if (message.getClass() != RegisterMessage.class) {
            throw new UnsupportedOperationException("Not a register message!");
        }
        var reg = (RegisterMessage)message;
        var name = reg.getName() != null ? reg.getName() : UUID.randomUUID().toString();
        return new RegisterResponseMessage(true, name);
    }
}
