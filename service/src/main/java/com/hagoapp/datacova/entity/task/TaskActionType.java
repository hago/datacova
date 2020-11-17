package com.hagoapp.datacova.entity.task;

import com.google.gson.annotations.SerializedName;

public enum TaskActionType {
    @SerializedName("0")
    DataImporting(0),
    @SerializedName("1")
    Verifying(1),
    @SerializedName("2")
    Distributing(2),
    @SerializedName("-1")
    IDLE(-1);

    private final int value;

    TaskActionType(int i) {
        for (TaskActionType t: TaskActionType.values()) {
            if (t.value == i) {
                value = i;
                return;
            }
        }
        value = -1;
    }

    public int getValue() {
        return value;
    }

    public static TaskActionType fromInt(int v) {
        for (TaskActionType a : TaskActionType.values()) {
            if (a.value == v) {
                return a;
            }
        }
        return TaskActionType.valueOf(String.valueOf(v));
    }

    public static TaskActionType fromString(String v) {
        for (TaskActionType t: TaskActionType.values()) {
            if (t.name().equalsIgnoreCase(v)) {
                return t;
            }
        }
        return IDLE;
    }
}
