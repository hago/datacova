/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.file;

import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * The factory class to create file store based on config.
 *
 * @author suncjs
 * @since 0.5
 */
@Slf4j
public class FileStoreFactory {

    private FileStoreFactory() {
    }

    private static final Map<String, Constructor<? extends FileStore>> fileStoreMaps = new HashMap<>(4);

    public static void register(String... namespaces) {
        for (var namespace : namespaces) {
            var r = new Reflections(namespace, Scanners.SubTypes);
            var configs = r.getSubTypesOf(FileStore.class);
            for (var config : configs) {
                var a = config.getAnnotation(FsScheme.class);
                if (a == null) {
                    log.error("Sub FileStore: {} is not annotated with FsSchema correctly", config.getCanonicalName());
                } else {
                    try {
                        var constructor = config.getConstructor(FsConfig.class);
                        fileStoreMaps.put(a.name(), constructor);
                        log.info("Sub FileStore: {} is registered with {}", config.getCanonicalName(), a.name());
                    } catch (NoSuchMethodException e) {
                        log.error("Sub FileStore: {} has no constructor with FsConfig", config.getCanonicalName());
                    }
                }
            }
        }
    }

    static {
        register("com.", "org.");
    }

    public static FileStore createFileStore(FsConfig config) {
        var anno = config.getClass().getAnnotation(FsScheme.class);
        if (anno == null) {
            log.error("{} is not a valid FsConfig", config.getClass().getCanonicalName());
            return null;
        }
        var scheme = anno.name();
        var constructor = fileStoreMaps.get(scheme);
        if (constructor == null) {
            log.error("{} has no registered FileStore", scheme);
            return null;
        }
        try {
            return constructor.newInstance(config);
        } catch (InstantiationException | RuntimeException | IllegalAccessException | InvocationTargetException e) {
            log.error("Create FileStore for {} failed: {}", scheme, e.getMessage());
            return null;
        }
    }

}
