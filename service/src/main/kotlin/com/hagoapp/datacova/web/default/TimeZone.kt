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

class TimeZone {
    @WebEndPoint(
        path = "/api/default/timezones",
        methods = [HttpMethod.GET],
        authTypes = [AuthType.UserToken]
    )
    fun getTimeZones(context: RoutingContext) {
        val zones = localizedTimeZones()
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to zones
            )
        )
    }

    private fun localizedTimeZones(): List<TimeZoneOffset> {
        val i = Instant.now()
        return ZoneId.getAvailableZoneIds().map {
            val z = ZoneId.of(it)
            TimeZoneOffset(
                z.toString(),
                z.rules.getOffset(i).totalSeconds
            )
        }.toSet().sortedBy { it.offset }
    }

    data class TimeZoneOffset(
        val name: String,
        val offset: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TimeZoneOffset

            if (name != other.name) return false
            if (offset != other.offset) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + offset
            return result
        }
    }
}
