package com.hagoapp.datacova.entity.task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hagoapp.datacova.CoVaException;

import java.util.Map;

public class TaskActionFactory {

    private interface Creator {
        TaskAction create(String json) throws CoVaException;

        default TaskAction create(Map<String, Object> map) throws CoVaException {
            return null;
        }
    }

    private static Gson gson = null;

    private synchronized static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder().create();
        }
        return gson;
    }

    public static TaskAction getTaskExecutionAction(String json) throws CoVaException {
        Gson gson = getGson();
        TaskAction act = gson.fromJson(json, TaskAction.class);
        return createTaskExecutionAction(act.getType(), json);
    }

    public static TaskAction getTaskExecutionAction(Map<String, Object> map) throws CoVaException {
        Gson gson = getGson();
        String json = gson.toJson(map);
        return getTaskExecutionAction(json);
    }

    public static TaskAction getTaskAction(Map<String, Object> map) throws CoVaException {
        Gson gson = getGson();
        String json = gson.toJson(map);
        return getTaskAction(json);
    }

    public static TaskAction getTaskAction(String json) throws CoVaException {
        Gson gson = getGson();
        TaskAction act = gson.fromJson(json, TaskAction.class);
        return createTaskAction(act.getType(), json);
    }

    private static TaskAction createTaskAction(TaskActionType type, String json) throws CoVaException {
        throw new CoVaException(String.format("Task Action Type %s not implemented", type.name()));
    }

    private static TaskAction createTaskExecutionAction(TaskActionType type, String json) throws CoVaException {
        throw new CoVaException(String.format("Task Action Type %s not implemented", type.name()));
    }
}
