package com.hagoapp.datacova.util.http;

import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseBuilder {

    private static final String HEADER_CORS = "Access-Control-Allow-Origin";
    private static final String HEADER_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private static final String HEADER_CONTENT_TYPE_JSON = "application/json";
    private static final String HEADER_CONTENT_TYPE_BINARY = "application/octet";
    private static final String HEADER_CONTENT_TYPE_HTML = "text/html";

    private HttpResponseStatus statusCode = HttpResponseStatus.OK;
    private String statusMessage = "";
    private final Map<String, String> headers = new HashMap<>();
    private String encoding = "UTF-8";
    private byte[] responseBody = new byte[0];

    public HttpResponseStatus getStatusCode() {
        return statusCode;
    }

    public ResponseBuilder setStatusCode(HttpResponseStatus statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public ResponseBuilder setStatus(int code) {
        HttpResponseStatus st = HttpResponseStatus.valueOf(code);
        return setStatusCode(st);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public ResponseBuilder setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        return this;
    }

    public String getEncoding() {
        return encoding;
    }

    public ResponseBuilder setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public ResponseBuilder setHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public ResponseBuilder addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    public ResponseBuilder enableCORS() {
        return this.enableCORS("*");
    }

    public ResponseBuilder enableCORS(String source) {
        this.headers.put(HEADER_CORS, source);
        return this;
    }

    public ResponseBuilder setResponseBody(String message) {
        try {
            this.responseBody = message.getBytes(this.encoding);
        } catch (UnsupportedEncodingException ex) {
            this.responseBody = message.getBytes(Charset.defaultCharset());
        }
        return this;
    }

    public ResponseBuilder setResponseBody(byte[] message) {
        this.responseBody = message;
        return this;
    }

    public ResponseBuilder setResponseBody(List<Object> message) {
        Gson gson = new Gson();
        String msg = gson.toJson(message);
        this.encoding = "UTF-8";
        return this.setResponseBody(msg);
    }

    public ResponseBuilder setResponseBody(Map<String, Object> message) {
        Gson gson = new Gson();
        String msg = gson.toJson(message);
        this.encoding = "UTF-8";
        return this.setResponseBody(msg);
    }

    public void send(RoutingContext routeContext) {
        HttpServerResponse rsp = routeContext.response();
        this.headers.forEach(rsp::putHeader);
        rsp.putHeader(HEADER_CONTENT_LENGTH, String.valueOf(this.responseBody.length));
        rsp.setStatusCode(this.statusCode.code());
        rsp.setStatusMessage(this.statusMessage);
        rsp.write(Buffer.buffer(this.responseBody));
        rsp.end();
    }

    public ResponseBuilder useJson() {
        this.headers.put(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_JSON);
        return this;
    }

    public ResponseBuilder useBinary() {
        this.headers.put(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_BINARY);
        return this;
    }

    public ResponseBuilder useHtml() {
        this.headers.put(HEADER_CONTENT_TYPE, HEADER_CONTENT_TYPE_HTML);
        return this;
    }
}
