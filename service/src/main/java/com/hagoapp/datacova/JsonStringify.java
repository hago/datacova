package com.hagoapp.datacova;

import com.google.gson.GsonBuilder;

public interface JsonStringify {
    default String toJson() {
        return new GsonBuilder().serializeNulls().setPrettyPrinting().create().toJson(this);
    }
}
