/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.user

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner

class UserAuthFactory private constructor() {
    companion object {
        private val factory0 = UserAuthFactory()

        fun getFactory(): UserAuthFactory {
            return factory0
        }
    }

    private val providers: MutableMap<String, UserAuthProvider> = mutableMapOf()

    init {
        val ref = Reflections("com.hagoapp.datacova", SubTypesScanner())
        ref.getSubTypesOf(UserAuthProvider::class.java).forEach { clz ->
            val provider = clz.getDeclaredConstructor().newInstance()
            providers[provider.getProviderName()] = provider
        }
    }

    fun availableAuthProviders(): List<UserAuthProvider> {
        return providers.values.toList()
    }

    fun getAuthProvider(name: String): UserAuthProvider? {
        return providers[name]
    }
}
