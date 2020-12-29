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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionActionDetail implements JsonStringify {
    private boolean succeeded;
    private List<Throwable> errors = new ArrayList<>();
    private List<String> messages = new ArrayList<>();
    private Map<Integer, List<String>> dataMessages = new HashMap<>();
    private TaskAction action;
    private long startTime;
    private Long endTime;

    public ExecutionActionDetail(TaskAction action) {
        this.action = action;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public void setErrors(List<Throwable> errors) {
        this.errors = errors;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public Map<Integer, List<String>> getDataMessages() {
        return dataMessages;
    }

    public void setDataMessages(Map<Integer, List<String>> dataMessages) {
        this.dataMessages = dataMessages;
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

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

}
