/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.default

import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

class TimeZone {
    @WebEndPoint(
        path = "/api/default/timezones",
        methods = [HttpMethod.GET],
        authTypes = [AuthType.UserToken]
    )
    fun getTimeZones(context: RoutingContext) {
        val localeStr = context.acceptableLanguages().firstOrNull()?.value()
        val locale = if (localeStr == null) Locale.getDefault() else createLocale(localeStr)
        val zones = localizedTimeZones(locale)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to zones
            )
        )
    }

    @WebEndPoint(
        path = "/api/default/timezones/:locale",
        methods = [HttpMethod.GET],
        authTypes = [AuthType.UserToken]
    )
    fun getTimeZonesLocale(context: RoutingContext) {
        val localeStr = context.pathParam("locale")
        val locale = createLocale(localeStr)
        val zones = localizedTimeZones(locale)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to zones
            )
        )
    }

    private fun createLocale(input: String): Locale {
        val parts = input.split("_", "-")
        return Locale(parts[0], if (parts.size > 1) parts[1] else "s")
    }

    private fun localizedTimeZones(locale: Locale): Map<String, Int> {
        val i = Instant.now()
        return ZoneId.getAvailableZoneIds().map { ZoneId.of(it) }
            .sortedBy { it.rules.getOffset(i).totalSeconds }
            .associate {
                Pair(
                    it.getDisplayName(TextStyle.FULL_STANDALONE, locale),
                    it.rules.getOffset(i).totalSeconds
                )
            }
    }
}
