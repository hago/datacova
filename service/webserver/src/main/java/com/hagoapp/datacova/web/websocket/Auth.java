/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.websocket;

import com.hagoapp.datacova.data.RedisCacheReader;
import com.hagoapp.datacova.user.UserInfo;
import com.hagoapp.datacova.util.http.RequestHelper;
import com.hagoapp.datacova.util.web.AuthUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.ServerWebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpCookie;
import java.util.List;

import static com.hagoapp.datacova.util.web.AuthUtils.LOGIN_COOKIE;

public class Auth {

    private static final Logger logger = LoggerFactory.getLogger(Auth.class);

    private Auth() {
    }

    public static UserInfo authenticate(ServerWebSocket serverWebSocket) {
        var token = authenticateCookie(serverWebSocket);
        if (token == null) {
            token = authenticateHeader(serverWebSocket);
        }
        if (token == null) {
            return null;
        }
        logger.debug("web socket token: {}", token);
        return RedisCacheReader.readDataInCacheOnly(token, UserInfo.class);
    }

    private static String authenticateCookie(ServerWebSocket serverWebSocket) {
        var headers = serverWebSocket.headers();
        var cookieStr = headers.get(HttpHeaderNames.COOKIE);
        logger.debug("web socket cookie: {}", cookieStr);
        if (cookieStr == null) {
            return null;
        }
        List<HttpCookie> cookies = RequestHelper.parseCookie(cookieStr);
        cookies.forEach(cookie -> logger.debug("cookie {}", cookie.getName()));
        var authCookie = cookies.stream()
                .filter(cookie -> cookie.getName().equals(LOGIN_COOKIE)).findFirst();
        return authCookie.map(HttpCookie::getValue).orElse(null);
    }

    private static String authenticateHeader(ServerWebSocket serverWebSocket) {
        var headers = serverWebSocket.headers();
        var authHeader = headers.get(HttpHeaders.AUTHORIZATION);
        return AuthUtils.Companion.parseAuthTokenHeader(authHeader);
    }
}
