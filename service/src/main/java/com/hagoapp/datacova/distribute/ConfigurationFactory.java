/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.distribute;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hagoapp.datacova.Application;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.MapSerializer;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationFactory {

    private static final Map<String, Class<? extends Configuration>> typeMap = new ConcurrentHashMap<String, Class<? extends Configuration>>();
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationFactory.class);

    static {
        Reflections r = new Reflections(Application.class.getPackageName(), Scanners.SubTypes);
        r.getSubTypesOf(Configuration.class).forEach(clz -> {
            try {
                var constructor = clz.getConstructor();
                var instance = constructor.newInstance();
                var existed = typeMap.put(instance.getType().toLowerCase(), clz);
                if (existed != null) {
                    logger.warn("Distribute action configuration type {} from {} is overwritten by {}", instance.getType(),
                            existed.getCanonicalName(), clz.getCanonicalName());
                }
            } catch (NoSuchMethodException | InstantiationException
                    | IllegalAccessException | InvocationTargetException e) {
                logger.error("Distribute action configuration type {} is not initialized: {}",
                        clz.getCanonicalName(), e.getMessage());
            }
        });
    }

    public static Configuration createConfiguration(Map<String, Object> content) throws CoVaException {
        try {
            return createConfiguration(content, MapSerializer.serializeMap(content));
        } catch (IOException e) {
            logger.error("Creation of verify action configuration from '{}' failed", content);
            return null;
        }
    }

    public static Configuration createConfiguration(Map<String, Object> content, String json) throws CoVaException {
        try {
            if (!content.containsKey("type")) {
                logger.error("No verify configuration type found: '{}'", json);
                return null;
            }
            var typeName = content.get("type").toString().toLowerCase();
            var clz = typeMap.get(typeName);
            if (clz == null) {
                logger.error("Corresponding class for distribute action configuration with type {} not found", typeName);
                return null;
            }
            var conf = new Gson().fromJson(json, clz);
            conf.checkValidity();
            return conf;
        } catch (JsonSyntaxException e) {
            logger.error("Creation of distribute action configuration from '{}' failed", json);
            return null;
        }
    }
}
