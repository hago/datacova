/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.action;

import com.google.gson.Gson;
import com.hagoapp.datacova.utility.CoVaException;
import com.hagoapp.datacova.lib.LibCoVa;
import com.hagoapp.datacova.utility.MapSerializer;
import com.hagoapp.datacova.utility.StackTraceWriter;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TaskActionFactory {

    private TaskActionFactory() {}

    private static final Map<Integer, Class<? extends TaskAction>> typeActionMap = new HashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(TaskActionFactory.class);

    static {
        Reflections r = new Reflections(LibCoVa.DEFAULT_NAMESPACE, Scanners.SubTypes);
        Set<Class<? extends TaskAction>> actionClasses = r.getSubTypesOf(TaskAction.class);
        actionClasses.forEach(actionClass -> {
            try {
                TaskAction template = actionClass.getConstructor().newInstance();
                var pre = typeActionMap.put(template.getType(), actionClass);
                logger.info("TaskAction: {} registered as type {}", actionClass.getCanonicalName(), template.getType());
                if (pre != null) {
                    logger.error("TaskAction: {} registered as type {} was overridden!", pre.getCanonicalName(), template.getType());
                }
            } catch (InstantiationException | IllegalAccessException |
                    InvocationTargetException | NoSuchMethodException e) {
                // won't happen
                logger.info("TaskAction: {} registered error: {}", actionClass.getCanonicalName(), e.getMessage());
                StackTraceWriter.write(e, logger);
            }
        });
    }

    @NotNull
    public static TaskAction getTaskAction(String json) throws CoVaException {
        try {
            return doGetTaskAction(json, MapSerializer.deserializeMap(json));
        } catch (IOException e) {
            throw new CoVaException("Task Action Error", e);
        }
    }

    public static TaskAction getTaskAction(Map<String, Object> map) throws CoVaException {
        try {
            return doGetTaskAction(MapSerializer.serializeMap(map), map);
        } catch (IOException e) {
            throw new CoVaException("Task Action Error", e);
        }
    }

    private static TaskAction doGetTaskAction(String json, Map<String, Object> map) throws CoVaException {
        try {
            var type = Integer.parseInt(map.get("type").toString());
            TaskAction action = new Gson().fromJson(json, typeActionMap.get(type));
            action.loadActualContent(map);
            return action;
        } catch (Exception e) {
            logger.error("Error for task action type: {}", map.get("type"));
            throw new CoVaException(String.format("Task Action Error: %s", e.getMessage()), e);
        }
    }
}
