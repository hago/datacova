/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.execution;

import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.entity.task.Task;

public class TaskExecution implements JsonStringify {
    private int id;
    private ExecutionFileInfo fileInfo;
    private int taskId;
    private Task task;
    private long addBy;
    private long addTime;
    private Long startTime;
    private Long endTime;
    private ExecutionDetail detail;
    private ExecutionStatus status;

    public ExecutionFileInfo getFileInfo() {
        return fileInfo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFileInfo(ExecutionFileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public long getAddBy() {
        return addBy;
    }

    public void setAddBy(long addBy) {
        this.addBy = addBy;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public ExecutionDetail getDetail() {
        return detail;
    }

    public void setDetail(ExecutionDetail detail) {
        this.detail = detail;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

}
