/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.connection;

import com.fasterxml.jackson.jr.ob.JSON;
import com.hagoapp.datacova.Application;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.CoVaLogger;
import com.hagoapp.datacova.MapSerializer;
import org.apache.logging.log4j.core.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ConnectionConfigFactory {

    private static final Map<Integer, Class<? extends ConnectionConfig>> connectionMap = new HashMap<>();
    private static final Logger logger = CoVaLogger.getLogger();

    static {
        Reflections r = new Reflections(Application.class.getPackageName(), new SubTypesScanner());
        Set<Class<? extends ConnectionConfig>> classes = r.getSubTypesOf(ConnectionConfig.class);
        classes.forEach(clz -> {
            try {
                ConnectionConfig sample = clz.getConstructor().newInstance();
                connectionMap.put(sample.getDbType(), clz);
            } catch (InstantiationException | IllegalAccessException |
                    InvocationTargetException | NoSuchMethodException e) {
                logger.error("connection type {} not recognized", clz.getCanonicalName());
            }
        });
    }

    public static ConnectionConfig getConnectionConfig(Map<String, Object> map) throws CoVaException {
        try {
            int type = Integer.parseInt(map.get("dbType").toString());
            return doGetConnectionConfig(MapSerializer.serializeMap(map), type);
        } catch (IOException e) {
            throw new CoVaException(String.format("Creating connection failed: %s, %s", e, map));
        }
    }

    public static ConnectionConfig getConnectionConfig(String json) throws CoVaException {
        try {
            Map<String, Object> map = MapSerializer.deserializeMap(json);
            return doGetConnectionConfig(json, Integer.parseInt(map.get("dbType").toString()));
        } catch (IOException e) {
            throw new CoVaException(String.format("Creating connection failed: %s, %s", e, json));
        }
    }

    private static ConnectionConfig doGetConnectionConfig(String json, int type) throws CoVaException {
        try {
            return JSON.std.beanFrom(connectionMap.get(type), json);
        } catch (IOException e) {
            throw new CoVaException(String.format("Creating connection failed: %s, type: %d, source: %s", e, type, json));
        }
    }

}
