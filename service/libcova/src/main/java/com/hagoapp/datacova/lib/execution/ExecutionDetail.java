/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.execution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.lib.action.TaskAction;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class ExecutionDetail implements JsonStringify {

    private long startTime = -1;
    private long endTime = -1;
    private final Map<Integer, ExecutionActionDetail> actionDetailMap = new HashMap<>();
    private Exception dataLoadingError;
    private int lineCount = -1;
    private final TaskExecution execution;

    public ExecutionDetail(TaskExecution execution) {
        this.execution = new Gson().fromJson(execution.toJson(), TaskExecution.class);
    }

    public TaskExecution getExecution() {
        return execution;
    }

    public boolean isSucceeded() {
        return (dataLoadingError == null) && this.actionDetailMap.values().stream().allMatch(ExecutionActionDetail::isSucceeded);
    }

    public boolean isAllActionsPerformed() {
        return this.actionDetailMap.size() == this.getExecution().getTask().getActions().size();
    }

    public static ExecutionDetail fromString(String s) {
        var gson = new GsonBuilder().create();
        return gson.fromJson(s, ExecutionDetail.class);
    }

    public void startTiming() {
        startTime = ZonedDateTime.now().toInstant().toEpochMilli();
    }

    public void endTiming() {
        endTime = ZonedDateTime.now().toInstant().toEpochMilli();
    }

    public long getTimeUsedMilliSeconds() throws CoVaException {
        if ((endTime < 0) && (execution != null)) {
            throw new CoVaException("Timing not started");
        }
        return endTime - startTime;
    }

    public ExecutionActionDetail addActionDetail(int actionIndex, TaskAction action) {
        var ead = new ExecutionActionDetail(action);
        actionDetailMap.put(actionIndex, ead);
        return ead;
    }

    public ExecutionActionDetail getActionDetail(int actionIndex) {
        return actionDetailMap.getOrDefault(actionIndex, null);
    }

    public Map<Integer, ExecutionActionDetail> getActionDetailMap() {
        return actionDetailMap;
    }

    public Exception getDataLoadingError() {
        return dataLoadingError;
    }

    public void setDataLoadingError(Exception dataLoadingError) {
        this.dataLoadingError = dataLoadingError;
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
    public String toString() {
        return "ExecutionDetail{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", actionDetailMap=" + actionDetailMap +
                ", dataLoadingError=" + dataLoadingError +
                ", lineCount=" + lineCount +
                ", execution=" + execution +
                '}';
    }

    public boolean hasDataMessages() {
        return !actionDetailMap.isEmpty() &&
                actionDetailMap.values().stream().anyMatch(ExecutionActionDetail::hasDataMessage);
    }
}
