package com.hagoapp.datacova.web;

import com.hagoapp.datacova.web.authentication.AuthType;
import io.vertx.core.http.HttpMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

public class WebHandler {
    private String path;
    private HttpMethod method;
    private List<AuthType> authenticatorTypes;
    private Class instanceClass;
    private Method function;
    private List<String> headers;
    private boolean pathAsRegex;
    private boolean blocking;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public List<AuthType> getAuthenticatorTypes() {
        return authenticatorTypes;
    }

    public void setAuthenticatorTypes(List<AuthType> authenticatorTypes) {
        this.authenticatorTypes = authenticatorTypes;
    }

    public Class getInstanceClass() {
        return instanceClass;
    }

    public void setInstanceClass(Class instanceClass) {
        this.instanceClass = instanceClass;
    }

    public Method getFunction() {
        return function;
    }

    public void setFunction(Method function) {
        this.function = function;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }

    public boolean isPathAsRegex() {
        return pathAsRegex;
    }

    public void setPathAsRegex(boolean pathAsRegex) {
        this.pathAsRegex = pathAsRegex;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public boolean equals(Object other) {
        if (!(other instanceof WebHandler)) {
            return false;
        }
        WebHandler wh = (WebHandler) other;
        return (this.path == wh.path) && (this.method == wh.method);
    }

    public String getKey() {
        return String.format("%s - %s", this.path, this.method);
    }

    public String toString() {
        return String.format("Method: %s, Path: %s, AuthTypes: %s, Class: %s, Method: %s",
                method.name(),
                path,
                authenticatorTypes.stream().map(Enum::name).collect(Collectors.joining(" | ")),
                instanceClass.getCanonicalName(),
                function.getName());
    }
}
