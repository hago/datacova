/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web;

import com.hagoapp.datacova.web.authentication.AuthType;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WebHandler {
    private String path;
    private String method;
    private List<AuthType> authenticatorTypes;
    private Class<?> instanceClass;
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        if (!MethodName.isValidName(method)) {
            throw new UnsupportedOperationException(String.format("%s is not a VALID http method.", method));
        }
        this.method = method;
    }

    public List<AuthType> getAuthenticatorTypes() {
        return authenticatorTypes;
    }

    public void setAuthenticatorTypes(List<AuthType> authenticatorTypes) {
        this.authenticatorTypes = authenticatorTypes;
    }

    public Class<?> getInstanceClass() {
        return instanceClass;
    }

    public void setInstanceClass(Class<?> instanceClass) {
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

    @Override
    public int hashCode() {
        return Objects.hash(path, method);
    }

    public boolean equals(Object other) {
        if (!(other instanceof WebHandler)) {
            return false;
        }
        WebHandler wh = (WebHandler) other;
        return (Objects.equals(this.path, wh.path)) && (Objects.equals(this.method, wh.method));
    }

    public String getKey() {
        return String.format("%s - %s", this.path, this.method);
    }

    public String toString() {
        return String.format("Method: %s, Path: %s, AuthTypes: %s, Class: %s, Method: %s",
                method,
                path,
                authenticatorTypes.stream().map(Enum::name).collect(Collectors.joining(" | ")),
                instanceClass.getCanonicalName(),
                function.getName());
    }
}
