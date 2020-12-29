/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.task;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.entity.action.TaskAction;
import com.hagoapp.datacova.execution.TaskActionFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Task implements JsonStringify {
    public int id;
    public String name;
    public String description = "";
    public int workspaceId;
    public List<TaskAction> actions;
    public TaskExtra extra;
    public String addBy;
    public long addTime;
    public String modifyBy;
    public Long modifyTime;

    private static Gson gson = null;

    private synchronized static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder().create();
        }
        return gson;
    }

    @NotNull
    @SuppressWarnings("unchecked")
    public static Task fromJson(String s) throws CoVaException, IOException {
        Gson gson = getGson();
        Task task = gson.fromJson(s, Task.class);
        Map<String, Object> map = JSON.std.mapFrom(s);
        List<Map<String, Object>> mapActions = (List<Map<String, Object>>) map.get("actions");
        List<TaskAction> actions = new ArrayList<>();
        for (int i = 0; i < task.actions.size(); i++) {
            actions.add(TaskActionFactory.getTaskAction(mapActions.get(i)));
        }
        task.actions = actions;
        return task;
    }

    @NotNull
    public static List<TaskAction> actionsFromJson(String s) throws CoVaException {
        Gson gson = getGson();
        TypeToken<ArrayList<Map<String, Object>>> token = new TypeToken<ArrayList<Map<String, Object>>>() {
        };
        List<Map<String, Object>> actionsMap = gson.fromJson(s, token.getType());
        List<TaskAction> actions = new ArrayList<>();
        for (Map<String, Object> map : actionsMap) {
            actions.add(TaskActionFactory.getTaskAction(map));
        }
        return actions;
    }

    @NotNull
    public String actions2Json() {
        Gson gson = getGson();
        return gson.toJson(this.actions);
    }

    public TaskExtra getExtra() {
        return extra;
    }

    public List<TaskAction> getActions() {
        return actions;
    }
}
