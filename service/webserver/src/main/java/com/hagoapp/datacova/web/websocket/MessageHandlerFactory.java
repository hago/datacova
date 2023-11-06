/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket;

import com.google.gson.Gson;
import com.hagoapp.datacova.Application;
import com.hagoapp.datacova.utility.MapSerializer;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageHandlerFactory {

    private static final Map<Integer, Constructor<? extends IMessageHandler>> handlerMap = new ConcurrentHashMap<>();
    private static final Map<Integer, Class<? extends ClientMessage>> typeMap = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(MessageHandlerFactory.class);

    static {
        var ref = new Reflections(Application.class.getPackageName(), Scanners.SubTypes);
        ref.getSubTypesOf(IMessageHandler.class).forEach(clz -> {
            try {
                var constructor = clz.getConstructor();
                var template = clz.getConstructor().newInstance();
                template.getHandledMessageTypes().forEach(type -> {
                    handlerMap.put(type, constructor);
                    if (typeMap.putIfAbsent(type, template.getMessageClass(type)) != null) {
                        logger.error("Class {} found to deal with conflicted web socket message {}", clz.getCanonicalName(), type);
                    } else {
                        logger.info("Class {} found to deal with web socket message {}", clz.getCanonicalName(), type);
                    }
                });
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                logger.error("trying to instantiation {} failed", clz.getCanonicalName());
            }
        });
    }

    private final String incomingMessage;
    private final int typeValue;

    public MessageHandlerFactory(String message) throws IOException {
        incomingMessage = message;
        Map<String, Object> messageObject = MapSerializer.deserializeMap(incomingMessage);
        var type = messageObject.get("type");
        if (type == null) {
            throw new IOException("no type defined");
        }
        typeValue = Integer.parseInt(type.toString());
    }

    public IMessageHandler createMessageHandler() {
        var creator = handlerMap.get(typeValue);
        if (creator == null) {
            logger.error("Web socket message handler for type {} not found", typeValue);
            return null;
        }
        try {
            return creator.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    public ClientMessage createClientMessage() {
        var clz = typeMap.get(typeValue);
        if (clz == null) {
            logger.error("Client message for type {} not found", typeValue);
            return null;
        }
        return new Gson().fromJson(incomingMessage, clz);
    }
}
