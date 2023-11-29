/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.dispatcher;

import com.hagoapp.datacova.dispatcher.handler.HeartbeatResponseHandler;
import com.hagoapp.datacova.dispatcher.handler.RegisterHandler;
import com.hagoapp.datacova.dispatcher.handler.WorkerDoneHandler;
import com.hagoapp.datacova.dispatcher.server.WorkerSpeaker;
import com.hagoapp.datacova.message.*;
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

    /**
     * The handler interface all server message handlers should conform.
     */
    public interface MessageHandler {
        /**
         * Implement processing of messages.
         *
         * @param speaker the speaker instance that receives the message
         * @param message the message
         * @return response message, null means response is unnecessary
         */
        Object handle(WorkerSpeaker speaker, Object message);
    }

    private final Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);
    private final MessageHandler defaultHandler = (speaker, registerMessage) -> {
        logger.warn("Message not recognized, skipped");
        return null;
    };
    private final Map<Class<?>, MessageHandler> handlerMap = Map.of(
            RegisterMessage.class, new RegisterHandler(),
            WorkerDoneMessage.class, new WorkerDoneHandler(),
            HeartBeatResponseMessage.class, new HeartbeatResponseHandler()
    );

    /**
     * This method checks incoming message and gives a nullable response. Null means no need of responding.
     *
     * @param data incoming message
     * @return outgoing response
     */
    public byte[] handle(WorkerSpeaker speaker, byte[] data) {
        try (var reader = new MessageReader()) {
            reader.update(data);
            var message = reader.parseMessage();
            logger.debug("processing message {}", message == null ? null : message.getClass().getCanonicalName());
            if (message == null) {
                defaultHandler.handle(speaker, null);
                return null;
            } else {
                var handler = handlerMap.get(message.getClass());
                if (handler == null) {
                    defaultHandler.handle(speaker, message);
                    return null;
                } else {
                    var responseMsg = handler.handle(speaker, message);
                    return responseMsg == null ? null : MessageWriter.toBytes(responseMsg);
                }
            }
        }
    }

}
