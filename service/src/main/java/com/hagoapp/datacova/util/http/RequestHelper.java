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
import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.ext.web.RoutingContext;

import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RequestHelper {

    public static byte[] readBodyBytes(RoutingContext context) {
        return context.getBody().getBytes();
    }

    public static String readBodyString(RoutingContext context) {
        String cType = context.request().getHeader(HttpHeaderNames.CONTENT_TYPE);
        Charset cs = null;
        if (cType != null) {
            Pattern p = Pattern.compile("charset=(.+)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(cType);
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
        return context.getBodyAsString(cs.name());
    }

    public static <T> T readBodyClass(RoutingContext context, Class<? extends T> clz) {
        String json = readBodyString(context);
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
        String cookieString = context.request().getHeader(HttpHeaderNames.COOKIE);
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
        String headerXForwardFor = request.getHeader("X-Forwarded-For");
        if (headerXForwardFor != null) {
            XForwardFor objectXForwardFor = XForwardFor.parse(headerXForwardFor);
            if (objectXForwardFor != null) {
                return objectXForwardFor.getClientIp();
            }
        }
        String forwardedHeader = request.getHeader("Forwarded");
        if (forwardedHeader != null) {
            Forwarded forwarded = Forwarded.parse(forwardedHeader);
            if (forwarded.getForClientIp() != null) {
                return forwarded.getForClientIp();
            }
        }
        return request.remoteAddress().host();
    }

    /**
     * Whether the request comes from internet.
     *
     * @param context request context
     * @return true if the request comes from internet, or false for intranet
     */
    public static boolean isFromInternet(RoutingContext context) {
        return List.of(
                "X-Forwarded-For", "Forwarded"
        ).stream().anyMatch(header -> context.request().getHeader(header) != null);
    }
}
