/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.message;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageFinder {
    private MessageFinder() {
    }

    private static final Map<Byte, Class<?>> messageMap = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(MessageFinder.class);

    static {
        var types = new Reflections(MessageFinder.class.getPackageName(), Scanners.TypesAnnotated)
                .getTypesAnnotatedWith(WorkerMessage.class);
        for (var t : types) {
            byte messageType = t.getAnnotation(WorkerMessage.class).type();
            if (messageMap.containsKey(messageType)) {
                logger.warn("type of {} and {} are conflicted: {}, skipped",
                        messageMap.get(messageType).getCanonicalName(), t.getCanonicalName(), messageType);
            } else {
                messageMap.put(messageType, t);
            }
        }
    }

    public static List<Object> getAllMessageTypes() {
        return Arrays.asList(messageMap.values().toArray());
    }

    public static Class<?> findMessageType(byte type) {
        return messageMap.get(type);
    }
}
