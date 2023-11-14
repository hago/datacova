/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher;

import com.hagoapp.datacova.dispatcher.handler.RegisterHandler;
import com.hagoapp.datacova.message.MessageReader;
import com.hagoapp.datacova.message.MessageWriter;
import com.hagoapp.datacova.message.RegisterMessage;
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
        Object handle(Object message);
    }

    private final Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);
    private final MessageHandler defaultHandler = (registerMessage) -> {
        logger.warn("Message not recognized, skipped");
        return null;
    };
    private final Map<Class<?>, MessageHandler> handlerMap = Map.of(
            RegisterMessage.class, new RegisterHandler()
    );

    /**
     * This method checks incoming message and gives a nullable response. Null means no need of responding.
     *
     * @param data incoming message
     * @return outgoing response
     */
    public byte[] handle(byte[] data) {
        try (var reader = new MessageReader()) {
            reader.update(data);
            var message = reader.parseMessage();
            logger.debug("processing message {}", message == null ? null : message.getClass().getCanonicalName());
            if (message == null) {
                defaultHandler.handle(null);
                return null;
            } else {
                var handler = handlerMap.get(message.getClass());
                if (handler == null) {
                    defaultHandler.handle(message);
                    return null;
                } else {
                    var responseMsg = handler.handle(message);
                    return responseMsg == null ? null : MessageWriter.toBytes(responseMsg);
                }
            }
        }
    }

}
