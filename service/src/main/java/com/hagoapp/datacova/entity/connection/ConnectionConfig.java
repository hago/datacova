package com.hagoapp.datacova.entity.connection;

import com.fasterxml.jackson.jr.ob.JSON;
import com.hagoapp.datacova.JsonStringify;

import java.io.IOException;
import java.util.Map;

public class ConnectionConfig implements JsonStringify {
    private int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Map<String, Object> toMap() throws IOException {
        return JSON.std.mapFrom(this.toJson());
    }
}
