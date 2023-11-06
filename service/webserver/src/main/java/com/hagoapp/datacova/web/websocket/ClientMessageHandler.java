/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket;

import com.hagoapp.datacova.message.MessageReader;
import com.hagoapp.datacova.message.MessageWriter;
import com.hagoapp.datacova.message.RegisterMessage;
import com.hagoapp.datacova.web.websocket.handler.RegisterHandler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The class defines associations between messages from clients and handler functions.
 *
 * @author suncjs
 * @since 0.5
 */
public class ClientMessageHandler {
    private ClientMessageHandler() {
    }

    private static ClientMessageHandler instance = null;

    public static ClientMessageHandler get() {
        if (instance == null) {
            instance = new ClientMessageHandler();
        }
        return instance;
    }

    public interface MessageHandler {
        Object handle(Object message, ServerWebSocket serverWebSocket);
    }

    private final Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);
    private final MessageHandler defaultHandler = (registerMessage, serverWebSocket) -> {
        logger.warn("Message not recognized, skipped");
        return null;
    };
    private final Map<Class<?>, MessageHandler> handlerMap = Map.of(
            RegisterMessage.class, new RegisterHandler()
    );

    /**
     * This method checks incoming message and gives a nullable response. Null means no need of responding.
     *
     * @param event incoming message
     * @return outgoing response
     */
    public byte[] handle(Buffer event, ServerWebSocket serverWebSocket) {
        try (var reader = new MessageReader()) {
            reader.update(event.getBytes());
            var message = reader.parseMessage();
            logger.debug("processing message {}", message == null ? null : message.getClass().getCanonicalName());
            var handler = message == null ? defaultHandler : handlerMap.get(message.getClass());
            if (handler == null) {
                defaultHandler.handle(null, serverWebSocket);
                return null;
            } else {
                var response = handler.handle(message, serverWebSocket);
                return response != null ? MessageWriter.toBytes(response) : null;
            }
        }
    }

}
