/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova;

import com.hagoapp.datacova.entity.Executor;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ExecutorManager {

    public static long CHECK_INTERVAL = 60 * 10 * 1000;
    private static final ExecutorManager manager = new ExecutorManager();

    public static ExecutorManager getManager() {
        return manager;
    }

    private final ConcurrentHashMap<String, ExecutorInfo> executorMap = new ConcurrentHashMap<>();
    private final Logger logger = CoVaLogger.getLogger();

    private ExecutorManager() {
        TimerTask checker = new TimerTask() {
            @Override
            public void run() {
                logger.debug("executor checking...");
                var keys = new ArrayList<>(executorMap.keySet());
                var n = Instant.now().toEpochMilli();
                keys.forEach(key -> executorMap.compute(key, (k, existed) -> {
                    if ((existed != null) && (existed.lastActiveTime + CHECK_INTERVAL * 5 < n)) {
                        return null;
                    } else {
                        return existed;
                    }
                }));
            }
        };
        Timer timer = new Timer();
        timer.schedule(checker, 2 * 1000, CHECK_INTERVAL);
    }

    public void registerExecutor(Executor executor) {
        executorMap.put(executor.toString(), new ExecutorInfo(executor));
    }

    public void keepAlive(Executor executor) {
        executorMap.compute(executor.toString(), (k, existed) -> {
            if (existed != null) {
                existed.lastActiveTime = Instant.now().toEpochMilli();
                return existed;
            } else {
                return new ExecutorInfo(executor);
            }
        });
    }

    public static class ExecutorInfo {
        private Executor executor;
        private long addTime = Instant.now().toEpochMilli();
        private long lastActiveTime = Instant.now().toEpochMilli();

        public Executor getExecutor() {
            return executor;
        }

        public void setExecutor(Executor executor) {
            this.executor = executor;
        }

        public long getAddTime() {
            return addTime;
        }

        public void setAddTime(long addTime) {
            this.addTime = addTime;
        }

        public long getLastActiveTime() {
            return lastActiveTime;
        }

        public void setLastActiveTime(long lastActiveTime) {
            this.lastActiveTime = lastActiveTime;
        }

        public ExecutorInfo(Executor executor) {
            this.executor = executor;
        }
    }
}
