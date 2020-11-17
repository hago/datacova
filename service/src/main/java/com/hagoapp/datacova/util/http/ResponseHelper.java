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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseHelper {
    public static boolean checkMethod(RoutingContext routeContext, HttpMethod method) {
        return routeContext.request().method().equals(method);
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status) {
        sendResponse(routeContext, status, "");
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status, String message) {
        sendResponse(routeContext, status, message, "UTF-8");
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status,
                                    Map<String, Object> message) {
        Gson gson = createGson();
        String msg = gson.toJson(message);
        Map<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
        }};
        sendResponse(routeContext, status, headers, msg, "UTF-8");
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus
            status, Map<String, String> headers, Map<String, Object> message) {
        Gson gson = createGson();
        String msg = gson.toJson(message);
        Map<String, String> h = new HashMap<>(headers);
        h.put("Content-Type", "application/json");
        sendResponse(routeContext, status, h, msg, "UTF-8");
    }

    private static Gson createGson() {
        return new GsonBuilder().serializeNulls().create();
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status, List<Object> message) {
        Gson gson = createGson();
        String msg = gson.toJson(message);
        Map<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
        }};
        sendResponse(routeContext, status, headers, msg, "UTF-8");
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus status, String message, String
            encoding) {
        sendResponse(routeContext, status, null, message, encoding);
    }

    public static void sendResponse(RoutingContext routeContext, HttpResponseStatus
            status, Map<String, String> headers, String message, String encoding) {
        Charset cs = Charset.forName(encoding);
        byte[] bytes = message.getBytes(cs);
        sendResponse(routeContext, status, headers, bytes);
    }

    public static <T> void sendResponse(RoutingContext routeContext, HttpResponseStatus status, T content) {
        Map<String, String> headers = new HashMap<String, String>() {{
            put("Content-Type", "application/json");
        }};
        sendResponse(routeContext, status, headers, content);
    }

    public static <T> void sendResponse(RoutingContext routeContext, HttpResponseStatus
            status, Map<String, String> headers, T content) {
        if (content instanceof byte[]) {
            byte[] buffer = (byte[]) content;
            HttpServerResponse rsp = routeContext.response();
            if (headers != null) headers.forEach(rsp::putHeader);
            rsp.putHeader("Content-Length", String.valueOf(buffer.length));
            rsp.setStatusCode(status.code());
            rsp.write(Buffer.buffer(buffer));
        } else {
            Gson gson = createGson();
            String msg = gson.toJson(content);
            HashMap<String, String> allHeaders = new HashMap<>(headers);
            allHeaders.put("Content-Type", "application/json");
            sendResponse(routeContext, status, allHeaders, msg, "UTF-8");
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
