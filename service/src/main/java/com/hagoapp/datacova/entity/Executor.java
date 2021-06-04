/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hagoapp.datacova.JsonStringify;

import java.util.ArrayList;
import java.util.List;

public class Executor implements JsonStringify {
    private String name;
    private String url;
    private final List<Integer> executionsRunning = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Integer> getExecutionsRunning() {
        return executionsRunning;
    }

    @Override
    public String toString() {
        return "Executor{" +
                "name='" + name + '\'' +
                ", iPAddress='" + url + '\'' +
                '}';
    }

    public static Executor fromJson(String json) {
        try {
            return new Gson().fromJson(json, Executor.class);
        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }
}
