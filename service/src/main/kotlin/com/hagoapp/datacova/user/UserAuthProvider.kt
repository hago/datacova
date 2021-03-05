/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.user

import io.vertx.ext.web.RoutingContext

interface UserAuthProvider {
    fun authenticate(context: RoutingContext): UserInfo?
    fun getProviderName(): String
    fun isValidUserId(userId: String): Boolean {
        return getUserInfo(userId) != null
    }

    fun getUserInfo(userId: String): UserInfo?
}
