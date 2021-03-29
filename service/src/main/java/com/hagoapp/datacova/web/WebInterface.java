/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
        return true;
    }

    Map<HttpMethod, Handler> requestHandlers();

    default List<AuthType> getAuthTypes() {
        return List.of(AuthType.Anonymous);
    }

    default List<String> requireHeaders() {
        return new ArrayList<>();
    }
}
