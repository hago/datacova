/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.execution;

import com.google.gson.Gson;
import com.hagoapp.datacova.utility.CoVaException;
import com.hagoapp.datacova.utility.JsonStringify;
import com.hagoapp.datacova.lib.task.Task;
import com.hagoapp.datacova.utility.MapSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Definition of one execution of a task, it keeps the snapshot of the task at the moment of scheduling.
 *
 * @author suncjs
 * @since 0.1
 */
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

    public static TaskExecution loadFromJson(String json) throws CoVaException {
        if (json == null) {
            return null;
        }
        try {
            var map = MapSerializer.deserializeMap(json);
            return fromMap(map, json);
        } catch (IOException e) {
            throw new CoVaException("Deserialize TaskExecution fail", e);
        }
    }

    @SuppressWarnings("unchecked")
    private static TaskExecution fromMap(Map<String, Object> map, String json) throws CoVaException, IOException {
        var taskJson = MapSerializer.serializeMap((Map<String, Object>) map.get("task"));
        var taskExecution = new Gson().fromJson(json, TaskExecution.class);
        var task = Task.fromJson(taskJson);
        taskExecution.setTask(task);
        return taskExecution;
    }

    public static List<TaskExecution> loadListFromJson(String json) throws CoVaException {
        if (json == null) {
            return null;
        }
        try {
            var list = MapSerializer.deserializeList(json);
            var ret = new ArrayList<TaskExecution>();
            for (var map : list) {
                var mapJson = MapSerializer.serializeMap(map);
                ret.add(fromMap(map, mapJson));
            }
            return ret;
        } catch (IOException e) {
            throw new CoVaException("Deserialize TaskExecution fail", e);
        }
    }

}
