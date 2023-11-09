/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseHelper {

    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";
    private static final String JSON_CONTENT_TYPE = "application/json";

    private ResponseHelper() {
    }

    public static boolean checkMethod(RoutingContext routeContext, HttpMethod method) {
        return routeContext.request().method().equals(method);
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status) {
        sendResponse(routeContext, status, "");
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status, String message) {
        sendResponse(routeContext, status, message, StandardCharsets.UTF_8.name());
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status,
                                    Map<String, Object> message) {
        var gson = createGson();
        var msg = gson.toJson(message);
        var headers = Map.of(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE);
        sendResponse(routeContext, status, headers, msg, StandardCharsets.UTF_8.name());
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus
            status, Map<String, String> headers, Map<String, Object> message) {
        var gson = createGson();
        var msg = gson.toJson(message);
        var h = new HashMap<>(headers);
        h.put(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE);
        sendResponse(routeContext, status, h, msg, StandardCharsets.UTF_8.name());
    }

    private static Gson createGson() {
        return new GsonBuilder().serializeNulls().create();
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status, List<Object> message) {
        var gson = createGson();
        var msg = gson.toJson(message);
        var headers = Map.of(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE);
        sendResponse(routeContext, status, headers, msg, StandardCharsets.UTF_8.name());
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status, String message, String
            encoding) {
        sendResponse(routeContext, status, null, message, encoding);
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus
            status, Map<String, String> headers, String message, String encoding) {
        var cs = Charset.forName(encoding);
        var bytes = message.getBytes(cs);
        sendResponse(routeContext, status, headers, bytes);
    }

    public static <T> void sendResponse(RoutingContext routeContext, HttpResponseStatus status, T content) {
        Map<String, String> headers = Map.of(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE);
        sendResponse(routeContext, status, headers, content);
    }

    public static <T> void sendResponse(RoutingContext routeContext, HttpResponseStatus
            status, Map<String, String> headers, T content) {
        if (content instanceof byte[]) {
            byte[] buffer = (byte[]) content;
            HttpServerResponse rsp = routeContext.response();
            if (headers != null) headers.forEach(rsp::putHeader);
            rsp.putHeader(CONTENT_TYPE_HEADER, String.valueOf(buffer.length));
            rsp.putHeader(CONTENT_LENGTH_HEADER, String.valueOf(buffer.length));
            rsp.setStatusCode(status.code());
            rsp.write(Buffer.buffer(buffer));
        } else {
            var allHeaders = new HashMap<>(headers);
            allHeaders.put(CONTENT_TYPE_HEADER, JSON_CONTENT_TYPE);
            var gson = createGson();
            var msg = gson.toJson(content);
            sendResponse(routeContext, status, allHeaders, msg, StandardCharsets.UTF_8.name());
        }
    }

    public static void respondError(RoutingContext routeContext, HttpResponseStatus status, String message) {
        respondError(routeContext, status, message, status.code(), null);
    }

    public static void respondError(RoutingContext routeContext, HttpResponseStatus status, String message, int code) {
        respondError(routeContext, status, message, code, null);
    }

    public static void respondError(RoutingContext routeContext, HttpResponseStatus status, String message, Object
            data) {
        respondError(routeContext, status, message, status.code(), data);
    }

    public static void respondError(RoutingContext routeContext, HttpResponseStatus status, String message,
                                    int code, Object data) {
        Map<String, Object> rsp = new HashMap<>();
        rsp.put("code", code);
        Map<String, Object> rspData = new HashMap<>();
        rspData.put("message", message);
        rspData.put("data", data);
        rsp.put("error", rspData);
        new ResponseBuilder().useJson().setStatusCode(status).setResponseBody(rsp).send(routeContext);
    }
}
