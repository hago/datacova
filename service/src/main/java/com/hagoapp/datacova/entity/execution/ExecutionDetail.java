/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.execution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.entity.action.TaskAction;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionDetail implements JsonStringify {
    private boolean succeeded = false;
    private long startTime = -1;
    private long endTime = -1;
    private final Map<Integer, ExecutionActionDetail> actionMap = new HashMap<>();
    private final List<Exception> errors = new ArrayList<>();
    private int lineCount = -1;

    public boolean isSucceeded() {
        succeeded = this.errors.isEmpty() && this.actionMap.values().stream().allMatch(ExecutionActionDetail::isSucceeded);
        return succeeded;
    }

    public static ExecutionDetail fromString(String s) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(s, ExecutionDetail.class);
    }

    public void startTiming() {
        startTime = ZonedDateTime.now().toInstant().toEpochMilli();
    }

    public void endTiming() {
        endTime = ZonedDateTime.now().toInstant().toEpochMilli();
    }

    public long getTimeUsedMilliSeconds() throws CoVaException {
        if (endTime < 0) {
            throw new CoVaException("Timing not started");
        }
        return endTime - startTime;
    }

    public ExecutionActionDetail addActionDetail(int actionIndex, TaskAction action) {
        ExecutionActionDetail ead = new ExecutionActionDetail(action);
        actionMap.put(actionIndex, ead);
        return ead;
    }

    public void addError(Exception error) {
        errors.add(error);
    }

    public List<Exception> getErrors() {
        return errors;
    }

    public ExecutionActionDetail getActionDetail(int actionIndex) {
        return actionMap.getOrDefault(actionIndex, null);
    }

    public Map<Integer, ExecutionActionDetail> getActionMap() {
        return actionMap;
    }

    public int getLineCount() {
        return lineCount;
    }

    public void setLineCount(int v) {
        lineCount = v;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @Override
    public String toJson() {
        isSucceeded();
        return JsonStringify.super.toJson();
    }
}
