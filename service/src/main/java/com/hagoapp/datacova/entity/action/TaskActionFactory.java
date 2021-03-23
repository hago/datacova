/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action;

import com.google.gson.Gson;
import com.hagoapp.datacova.Application;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.CoVaLogger;
import com.hagoapp.datacova.MapSerializer;
import com.hagoapp.datacova.util.StackTraceWriter;
import org.apache.logging.log4j.core.Logger;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TaskActionFactory {

    private static final Map<TaskActionType, Class<? extends TaskAction>> typeActionMap = new HashMap<>();

    private static final Logger logger = CoVaLogger.getLogger();

    static {
        Reflections r = new Reflections(Application.class.getPackageName(), new SubTypesScanner());
        Set<Class<? extends TaskAction>> actionClasses = r.getSubTypesOf(TaskAction.class);
        actionClasses.forEach(actionClass -> {
            try {
                TaskAction template = actionClass.getConstructor().newInstance();
                var pre = typeActionMap.put(template.getType(), actionClass);
                logger.info("TaskAction: {} registered as type {}", actionClass.getCanonicalName(), template.getType());
                if (pre != null) {
                    logger.error("TaskAction: {} registered as type {} was overrode!", pre.getCanonicalName(), template.getType());
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
        String actionName = map.get("type").toString();
        try {
            TaskActionType type = TaskActionType.valueOf(actionName);
            return new Gson().fromJson(json, typeActionMap.get(type));
        } catch (Exception e) {
            logger.error("Error for type: {}", actionName);
            throw new CoVaException("Task Action Error", e);
        }
    }
}
