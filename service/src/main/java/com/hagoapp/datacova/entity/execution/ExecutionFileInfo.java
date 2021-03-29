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
import com.hagoapp.f2t.datafile.FileType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

public class ExecutionFileInfo extends FileInfo implements JsonStringify {

    private String originalName;
    private long size;

    public ExecutionFileInfo(@NotNull String initFilename) {
        super(initFilename);
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
        calcType();
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

    @Override
    protected void calcType() {
        if (originalName == null) {
            return;
        }
        var i = originalName.lastIndexOf('.');
        if (i < 0) {
            type = FileType.Unknown;
        } else switch (originalName.substring(i).toLowerCase()) {
            case ".csv":
                type = FileType.CSV;
                break;
            case ".xls":
                type = FileType.Excel;
                break;
            case ".xlsx":
                type = FileType.ExcelOpenXML;
                break;
            default:
                type = FileType.Unknown;
        }

    }

    public static ExecutionFileInfo getFileInfo(String s) throws IOException {
        Gson gson = new GsonBuilder().create();
        ExecutionFileInfo eai = gson.fromJson(s, ExecutionFileInfo.class);
        Map<String, Object> map = JSON.std.mapFrom(s);
        return eai;
    }
}
