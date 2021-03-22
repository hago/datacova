/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action;

public class TaskAction {
    protected TaskActionType type;
    private TaskActionExtra extra = new TaskActionExtra();
    private String name;
    private String description = "";

    public TaskAction() {
        //
    }

    public TaskActionType getType() {
        return type;
    }

    public TaskActionExtra getExtra() {
        return extra;
    }

    public void setExtra(TaskActionExtra extra) {
        this.extra = extra;
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

}
