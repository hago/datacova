/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.util.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hagoapp.datacova.utility.http.Forwarded;
import com.hagoapp.datacova.utility.http.XForwardFor;
import edazdarevic.commons.net.CIDRUtils;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;

import java.net.HttpCookie;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RequestHelper {
    private RequestHelper() {
    }

    public static byte[] readBodyBytes(RoutingContext context) {
        return context.body().buffer().getBytes();
    }

    public static String readBodyString(RoutingContext context) {
        String cType = context.request().getHeader(HttpHeaderNames.CONTENT_TYPE);
        Charset cs = null;
        if (cType != null) {
            var p = Pattern.compile("charset=(.+)", Pattern.CASE_INSENSITIVE);
            var m = p.matcher(cType);
            if (m.find()) {
                try {
                    cs = Charset.forName(m.group());
                } catch (Exception e) {
                    // do nothing continue next attempt
                }
            }
        }
        if (cs == null) {
            String enc = context.request().getHeader(HttpHeaderNames.CONTENT_ENCODING);
            try {
                cs = Charset.forName(enc);
            } catch (Exception e) {
                // do nothing continue next attempt
            }
        }
        if (cs == null) {
            cs = StandardCharsets.UTF_8;
        }
        return context.body().asString(cs.name());
    }

    public static <T> T readBodyClass(RoutingContext context, Class<? extends T> clz) {
        var json = readBodyString(context);
        if (json == null) {
            return null;
        } else {
            try {
                return new Gson().fromJson(json, clz);
            } catch (JsonSyntaxException e) {
                return null;
            }
        }
    }

    public static List<HttpCookie> parseCookie(RoutingContext context) {
        var cookieString = context.request().getHeader(HttpHeaderNames.COOKIE);
        return parseCookie(cookieString);
    }

    public static List<HttpCookie> parseCookie(String cookieString) {
        if (cookieString == null) {
            return List.of();
        }
        return Arrays.stream(cookieString.split(";")).map(element -> {
            var parts = element.split("=");
            return parts.length == 2 ? new HttpCookie(parts[0], parts[1]) : null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static String getBaseUrl(RoutingContext context) {
        return String.format("%s://%s", context.request().scheme(), context.request());
    }

    public static String getRemoteIp(RoutingContext context) {
        var request = context.request();
        return getRemoteIp(context.request().headers(), request.remoteAddress().host());
    }

    public static String getRemoteIp(MultiMap headers) {
        return getRemoteIp(headers, null);
    }

    public static String getRemoteIp(MultiMap headers, String defaultIp) {
        String headerXForwardFor = headers.get("X-Forwarded-For");
        if (headerXForwardFor != null) {
            var objectXForwardFor = XForwardFor.parse(headerXForwardFor);
            if (objectXForwardFor != null) {
                return objectXForwardFor.getClientIp();
            }
        }
        var forwardedHeader = headers.get("Forwarded");
        if (forwardedHeader != null) {
            var forwarded = Forwarded.parse(forwardedHeader);
            if (forwarded.getForClientIp() != null) {
                return forwarded.getForClientIp();
            }
        }
        return defaultIp;
    }

    /**
     * Whether the request is forwarded by any reverse proxies.
     *
     * @param context request context
     * @return true if the request comes from internet, or false for intranet
     */
    public static boolean requestForwarded(RoutingContext context) {
        return Stream.of("X-Forwarded-For", "Forwarded")
                .anyMatch(header -> context.request().getHeader(header) != null);
    }

    private static final List<String> INTRANET_RANGES = List.of(
            "192.168.0.0/24", "10.0.0.0/8", "172.1.0.0/11", "127.0.0.0/8"
    );

    /**
     * Whether the request comes from internet.
     *
     * @param context request context
     * @return true if the request comes from internet, or false for intranet
     */
    public static boolean isFromInternet(RoutingContext context) {
        String ip = getRemoteIp(context);
        return INTRANET_RANGES.stream().noneMatch(subnet -> {
            try {
                return new CIDRUtils(subnet).isInHostsRange(ip);
            } catch (UnknownHostException e) {
                return false;
            }
        });
    }

    /**
     * Retrieve user agent
     *
     * @param context request context
     * @return agent string
     */
    public static String getUserAgent(RoutingContext context) {
        return getUserAgent(context.request().headers());
    }

    /**
     * Retrieve user agent
     *
     * @param map headers from request
     * @return agent string
     */
    public static String getUserAgent(MultiMap map) {
        return map.get(HttpHeaderNames.USER_AGENT);
    }
}
