/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.execution;

import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.entity.action.TaskAction;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionActionDetail implements JsonStringify {
    private final Map<Integer, List<DataMessage>> dataMessages = new HashMap<>();
    private Exception error;
    private TaskAction action;
    private final long startTime = Instant.now().toEpochMilli();
    private Long endTime;

    public boolean isSucceeded() {
        return error == null;
    }

    public ExecutionActionDetail(TaskAction action) {
        this.action = action;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }

    public Map<Integer, List<DataMessage>> getDataMessages() {
        return dataMessages;
    }

    public TaskAction getAction() {
        return action;
    }

    public void setAction(TaskAction action) {
        this.action = action;
    }

    public long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void end() {
        this.endTime = Instant.now().toEpochMilli();
    }

    @Override
    public String toString() {
        return "ExecutionActionDetail{" +
                "dataMessages=" + dataMessages +
                ", error=" + error +
                ", action=" + action +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public boolean hasDataMessage() {
        return !dataMessages.isEmpty();
    }
}
