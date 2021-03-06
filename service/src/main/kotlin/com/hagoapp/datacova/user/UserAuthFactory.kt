/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.user

import com.hagoapp.datacova.CoVaLogger
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
        val logger = CoVaLogger.getLogger()
        ref.getSubTypesOf(UserAuthProvider::class.java).forEach { clz ->
            logger.debug("Authentication provider found: " + clz.canonicalName)
            val provider = clz.getDeclaredConstructor().newInstance()
            providers[provider.getProviderName()] = provider
        }
    }

    fun availableAuthProviders(): List<UserAuthProvider> {
        return providers.values.toList()
    }

    fun getAuthProvider(name: String?): UserAuthProvider {
        return when {
            name == null -> providers[LocalUserProvider.PROVIDER_NAME]!!
            !providers.containsKey(name) -> providers[LocalUserProvider.PROVIDER_NAME]!!
            else -> providers.getValue(name)
        }
    }

    fun getAuthProvider(id: Int): UserAuthProvider {
        val p = providers.values.firstOrNull { it.getProviderType() == id }
        return p ?: providers.getValue(LocalUserProvider.PROVIDER_NAME)
    }
}
