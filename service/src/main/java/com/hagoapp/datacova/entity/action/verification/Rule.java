/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.verification;

import com.google.gson.Gson;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.datacova.MapSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

public class Rule implements JsonStringify {
    private long id;
    private String name;
    private String description;
    private Configuration ruleConfig;
    private Integer workspaceId;
    private long addBy;
    private long addTime;
    private long modifyBy;
    private long modifyTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public Configuration getRuleConfig() {
        return ruleConfig;
    }

    public void setRuleConfig(Configuration ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public Integer getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Integer workspaceId) {
        this.workspaceId = workspaceId;
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

    public long getModifyBy() {
        return modifyBy;
    }

    public void setModifyBy(long modifyBy) {
        this.modifyBy = modifyBy;
    }

    public long getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(long modifyTime) {
        this.modifyTime = modifyTime;
    }

    @NotNull
    public static Rule fromJson(@NotNull String json) throws IOException {
        Map<String, Object> map = MapSerializer.deserializeMap(json);
        Rule rule = new Gson().fromJson(json, Rule.class);
        rule.ruleConfig = ConfigurationFactory.createConfiguration(map, json);
        return rule;
    }
}
