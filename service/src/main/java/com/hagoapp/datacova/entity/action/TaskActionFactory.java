/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.Application;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.MapSerializer;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class TaskActionFactory {

    private static final Map<Integer, Class<? extends TaskAction>> typeActionMap = new HashMap<>();

    static {
        Reflections r = new Reflections(Application.class.getPackageName(), new SubTypesScanner());
        Set<Class<? extends TaskAction>> actionClasses = r.getSubTypesOf(TaskAction.class);
        actionClasses.forEach(actionClass -> {
            try {
                TaskAction sample = actionClass.getConstructor().newInstance();
                typeActionMap.put(sample.getType(), actionClass);
            } catch (InstantiationException | IllegalAccessException |
                    InvocationTargetException | NoSuchMethodException ignored) {
                // won't happen
            }
        });
    }

    private static Gson gson = null;

    private synchronized static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder().create();
        }
        return gson;
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
            int type = Integer.parseInt(map.get("type").toString());
            TaskAction ta = new Gson().fromJson(json, typeActionMap.get(type));
            ta.load(map);
            return ta;
        } catch (Exception e) {
            throw new CoVaException("Task Action Error", e);
        }
    }
}
