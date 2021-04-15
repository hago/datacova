/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket;

import com.hagoapp.datacova.data.RedisCacheReader;
import com.hagoapp.datacova.user.UserInfo;
import com.hagoapp.datacova.util.web.AuthUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.ServerWebSocket;

import java.net.HttpCookie;
import java.util.List;

import static com.hagoapp.datacova.util.web.AuthUtils.LOGIN_COOKIE;

public class Auth {
    public static UserInfo authenticate(ServerWebSocket serverWebSocket) {
        var token = authenticateCookie(serverWebSocket);
        if (token == null) {
            token = authenticateHeader(serverWebSocket);
        }
        if (token == null) {
            return null;
        }
        return RedisCacheReader.readCachedData(token, 1, params -> null, UserInfo.class);
    }

    private static String authenticateCookie(ServerWebSocket serverWebSocket) {
        var headers = serverWebSocket.headers();
        var cookieStr = headers.get(HttpHeaderNames.COOKIE);
        if (cookieStr == null) {
            return null;
        }
        List<HttpCookie> cookies = HttpCookie.parse(cookieStr);
        var authCookie = cookies.stream()
                .filter(cookie -> cookie.getName().equals(LOGIN_COOKIE)).findFirst();
        if (authCookie.isEmpty()) {
            return null;
        }
        return authCookie.get().getValue();
    }

    private static String authenticateHeader(ServerWebSocket serverWebSocket) {
        var headers = serverWebSocket.headers();
        var authHeader = headers.get(HttpHeaders.AUTHORIZATION);
        return AuthUtils.Companion.parseAuthTokenHeader(authHeader);
    }
}
