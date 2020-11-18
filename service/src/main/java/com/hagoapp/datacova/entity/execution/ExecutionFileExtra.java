/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.execution;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.execution.datafile.DataFileType;
import com.hagoapp.datacova.util.Utils;

import java.io.InvalidClassException;

public class ExecutionFileExtra {
    protected DataFileType type = DataFileType.Unknown;

    public DataFileType getType() {
        return type;
    }

    public void setType(DataFileType type) {
        this.type = type;
    }

    public static ExecutionFileExtra createExtra(String s) throws InvalidClassException {
        return createExtra(s, DataFileType.CSV);
    }

    public static ExecutionFileExtra createExtra(String s, ExecutionFileInfo info) throws InvalidClassException {
        if ((info.getOriginalName() == null) || info.getOriginalName().isBlank()) {
            throw new InvalidClassException("empty file originalName");
        }
        String ext = Utils.Companion.parseFileName(info.getOriginalName()).getExt();
        DataFileType t = DataFileType.getFromExtension(ext);
        return createExtra(s, t);
    }

    public static ExecutionFileExtra createExtra(String s, DataFileType defaultType) throws InvalidClassException {
        Gson gson = new GsonBuilder().create();
        ExecutionFileExtra e = gson.fromJson(s, ExecutionFileExtra.class);
        if ((e.type == null) || (e.type == DataFileType.Unknown)) {
            e.type = defaultType;
        }
        switch (e.type) {
            case CSV:
                return gson.fromJson(s, ExecutionFileExtraCSV.class);
            case Excel:
                return gson.fromJson(s, ExecutionFileExtraExcel.class);
            case ExcelOpenXML:
                return gson.fromJson(s, ExecutionFileExtraExcelOpenXML.class);
            default:
                throw new InvalidClassException(String.format("unknown data file type %s", e.type.name()));
        }
    }

}
