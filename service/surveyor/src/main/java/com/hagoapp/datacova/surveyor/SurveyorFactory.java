/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.surveyor;

import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.surveyor.surveyor.Surveyor;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


/**
 * The factory class helps to create appropriate <code>RuleConfig</code> sub-typed configuration classes from its JSON
 * string, or to create appropriate surveyor based on a specific config class.
 *
 * @author Chaojun Sun
 * @since 0.1
 */
public class SurveyorFactory {

    private SurveyorFactory() {
    }

    private static final Map<String, Class<? extends RuleConfig>> configurations = new HashMap<>();
    private static final Map<String, Class<? extends Surveyor>> processors = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SurveyorFactory.class);

    static {
        registerRuleConfiguration(SurveyorFactory.class.getPackageName());
    }

    public static void registerRuleConfiguration(String packageName) {
        var types = new Reflections(packageName, Scanners.SubTypes).getSubTypesOf(RuleConfig.class);
        for (var type : types) {
            try {
                var instance = type.getConstructor().newInstance();
                var typeName = instance.getConfigType();
                if (configurations.containsKey(typeName)) {
                    var oldClz = configurations.get(typeName);
                    logger.warn("Configuration type {} is replaced by {} for same config type name {}",
                            oldClz.getCanonicalName(), type.getCanonicalName(), typeName);
                }
                configurations.put(typeName, type);
                logger.info("Configuration type {} registered by class {}", typeName, type.getCanonicalName());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                logger.error("Error occurs while trying to instantiate {}", type.getCanonicalName());
                logger.error("Cause: {}", e.getMessage());
            }
        }
        var processorTypes = new Reflections(packageName, Scanners.SubTypes).getSubTypesOf(Surveyor.class);
        for (var type : processorTypes) {
            try (var instance = type.getConstructor().newInstance()) {
                var typeName = instance.getSupportedConfigType();
                if (processors.containsKey(typeName)) {
                    var oldClz = processors.get(typeName);
                    logger.warn("processor type {} is replaced by {} for same type name {}",
                            oldClz.getCanonicalName(), type.getCanonicalName(), typeName);
                }
                processors.put(typeName, type);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | IOException e) {
                logger.error("Error occurs while trying to instantiate {}", type.getCanonicalName());
                logger.error("cause: {}", e.getMessage());
            }
        }
    }

    public static RuleConfig createRuleConfig(Map<String, Object> map) {
        return createRuleConfig(new GsonBuilder().create().toJson(map));
    }

    public static RuleConfig createRuleConfig(InputStream stream) throws IOException {
        var buffer = stream.readAllBytes();
        var json = new String(buffer, StandardCharsets.UTF_8);
        return createRuleConfig(json);
    }

    public static RuleConfig createRuleConfig(String json) {
        var gson = new GsonBuilder().create();
        var baseClass = gson.fromJson(json, RuleConfig.class);
        var typeName = baseClass.getConfigType();
        var clz = configurations.get(typeName);
        if (clz == null) {
            logger.error("no rule config class found for name: {}", typeName);
            throw new UnsupportedOperationException(
                    String.format("no rule config class found for name: %s", typeName));
        }
        return gson.fromJson(json, clz);
    }

    public static Surveyor createSurveyor(Map<String, Object> map) {
        var config = createRuleConfig(map);
        return createSurveyor(config);
    }

    public static Surveyor createSurveyor(InputStream stream) throws IOException {
        var config = createRuleConfig(stream);
        return createSurveyor(config);
    }

    public static Surveyor createSurveyor(String json) {
        var config = createRuleConfig(json);
        return createSurveyor(config);
    }

    public static Surveyor createSurveyor(RuleConfig config) {
        var typeName = config.getConfigType();
        var clz = processors.get(typeName);
        if (clz == null) {
            logger.error("no rule processor class found for name: {}", typeName);
            throw new UnsupportedOperationException(
                    String.format("no processor config class found for name: %s", typeName));
        }
        try {
            return clz.getConstructor().newInstance().acceptConfiguration(config);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new UnsupportedOperationException(
                    String.format("error occurs while instantiating name: %s", typeName));
        }
    }
}
