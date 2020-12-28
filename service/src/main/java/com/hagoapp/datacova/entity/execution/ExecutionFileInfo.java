package com.hagoapp.datacova.entity.execution;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.JsonStringify;
import com.hagoapp.f2t.datafile.FileInfo;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

public class ExecutionFileInfo extends FileInfo implements JsonStringify {
    private String originalName;
    private long size;
    private ExecutionFileExtra extra;

    public ExecutionFileInfo(@NotNull String filename) {
        super(filename);
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public ExecutionFileExtra getExtra() {
        return extra;
    }

    public void setExtra(ExecutionFileExtra extra) {
        this.extra = extra;
    }

    public String getOriginalName() {
        return originalName;
    }

    public static ExecutionFileInfo getFileInfo(String s) throws IOException {
        Gson gson = new GsonBuilder().create();
        ExecutionFileInfo eai = gson.fromJson(s, ExecutionFileInfo.class);
        Map<String, Object> map = JSON.std.mapFrom(s);
        if (map.containsKey("extra")) {
            String extraStr = gson.toJson(map.get("extra"));
            eai.extra = ExecutionFileExtra.createExtra(extraStr, eai);
        }
        return eai;
    }

    public long getSize() {
        return size;
    }
}
