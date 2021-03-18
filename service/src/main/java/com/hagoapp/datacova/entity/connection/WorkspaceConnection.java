/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.connection;

import com.google.gson.Gson;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.MapSerializer;

import java.io.IOException;
import java.util.Map;

public class WorkspaceConnection implements JsonStringify {
    private int id;
    private String name;
    private String description;
    private int workspaceId;
    private ConnectionConfig configuration;
    private long addBy;
    private long addTime;
    private Long modifyBy;
    private Long modifyTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(int workspaceId) {
        this.workspaceId = workspaceId;
    }

    public ConnectionConfig getConfiguration() {
        return configuration;
    }

    public void setConfiguration(ConnectionConfig configuration) {
        this.configuration = configuration;
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

    public Long getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(Long modifyBy) {
        this.modifyBy = modifyBy;
    }

    public Long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Long modifyTime) {
        this.modifyTime = modifyTime;
    }

    @SuppressWarnings("unchecked")
    public static WorkspaceConnection load(String json) {
        Map<String, Object> map = null;
        try {
            map = MapSerializer.deserializeMap(json);
            Object o = map.get("configuration");
            Map<String, Object> configMap = (Map<String, Object>) map.get("configuration");
            WorkspaceConnection w = new Gson().fromJson(json, WorkspaceConnection.class);
            ConnectionConfig config = ConnectionConfigFactory.getConnectionConfig(configMap);
            w.setConfiguration(config);
            return w;
        } catch (IOException | CoVaException e) {
            return null;
        }
    }
}
