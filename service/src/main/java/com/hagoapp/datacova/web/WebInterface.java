package com.hagoapp.datacova.web;

import com.hagoapp.datacova.web.authentication.AuthType;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Any class that implements this method will be discovered as a web handler.
 */
public interface WebInterface {
    interface Handler {
        void handle(RoutingContext routeContext);
    }

    String getPath();

    default boolean isPathRegex() {
        return false;
    }

    default boolean isBlocking() {
        return false;
    }

    Map<HttpMethod, Handler> requestHandlers();

    default List<AuthType> getAuthTypes() {
        return List.of(AuthType.Anonymous);
    }

    default List<String> requireHeaders() {
        return new ArrayList<>();
    }
}
