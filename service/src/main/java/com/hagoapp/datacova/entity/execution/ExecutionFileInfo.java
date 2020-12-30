/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.execution;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.f2t.datafile.FileInfo;
import com.hagoapp.f2t.datafile.FileInfoReader;

import java.io.IOException;
import java.util.Map;

public class ExecutionFileInfo implements JsonStringify {
    private FileInfo dataFileInfo;
    private String originalName;
    private long size;

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public FileInfo getDataFileInfo() {
        return dataFileInfo;
    }

    public void setDataFileInfo(FileInfo dataFileInfo) {
        this.dataFileInfo = dataFileInfo;
    }

    public static ExecutionFileInfo getFileInfo(String s) throws IOException {
        Gson gson = new GsonBuilder().create();
        ExecutionFileInfo eai = gson.fromJson(s, ExecutionFileInfo.class);
        Map<String, Object> map = JSON.std.mapFrom(s);
        String fiString = gson.toJson(map.get("fileInfo"));
        eai.dataFileInfo = FileInfoReader.json2FileInfo(fiString);
        return eai;
    }
}
