/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShutDownManager {

    private ShutDownManager() {
    }

    private static final List<ShutDownWatcher> WATCHERS = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ShutDownManager.class);

    public static void watch(ShutDownWatcher watcher) {
        WATCHERS.add(watcher);
    }

    public static void closeWatchers() {
        var threads = WATCHERS.stream().map(watcher -> {
            var t = new Thread(() -> {
                try {
                    watcher.shutdown();
                    LOGGER.info("Shutdown {} successfully", watcher.getName());
                } catch (Exception e) {
                    LOGGER.error("Shutdown {} failed: {}", watcher.getName(), e.getMessage());
                }
            });
            t.setDaemon(true);
            return t;
        }).collect(Collectors.toList());
        threads.forEach(Thread::run);
        threads.forEach(t -> {
            try {
                t.join(1000 * 60L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }
}
