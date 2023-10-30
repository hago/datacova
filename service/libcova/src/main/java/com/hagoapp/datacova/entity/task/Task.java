/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.task;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jr.ob.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.entity.action.TaskAction;
import com.hagoapp.datacova.entity.action.TaskActionFactory;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Task implements JsonStringify {
    private int id;
    private String name;
    private String description = "";
    private int workspaceId;
    private List<TaskAction> actions;
    private TaskExtra extra;
    private long addBy;
    private long addTime;
    private Long modifyBy;
    private Long modifyTime;

    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();

    @NotNull
    @SuppressWarnings("unchecked")
    public static Task fromJson(String s) throws CoVaException, IOException {
        Task task = gson.fromJson(s, Task.class);
        Map<String, Object> map = JSON.std.mapFrom(s);
        List<Map<String, Object>> mapActions = (List<Map<String, Object>>) map.get("actions");
        List<TaskAction> actions = new ArrayList<>();
        for (Map<String, Object> mapAction : mapActions) {
            actions.add(TaskActionFactory.getTaskAction(mapAction));
        }
        task.actions = actions;
        return task;
    }

    @NotNull
    public static List<TaskAction> actionsFromJson(String s) throws CoVaException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType elemType = mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, elemType);
        List<Map<String, Object>> actionsMap = mapper.readValue(s, javaType);
        List<TaskAction> actions = new ArrayList<>();
        for (Map<String, Object> map : actionsMap) {
            actions.add(TaskActionFactory.getTaskAction(map));
        }
        return actions;
    }

    @NotNull
    public String actions2Json() {
        return gson.toJson(this.actions);
    }
}
