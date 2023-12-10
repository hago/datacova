/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.user

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.slf4j.LoggerFactory

object UserAuthFactory {

    private val providers: MutableMap<String, UserAuthProvider> = mutableMapOf()

    init {
        val ref = Reflections("com.hagoapp.datacova", Scanners.SubTypes)
        val logger = LoggerFactory.getLogger(UserAuthFactory::class.java)
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
            name == null -> providers.getValue(LocalUserProvider.PROVIDER_NAME)
            !providers.containsKey(name) -> providers.getValue(LocalUserProvider.PROVIDER_NAME)
            else -> providers.getValue(name)
        }
    }

    fun getAuthProvider(id: Int): UserAuthProvider {
        val p = providers.values.firstOrNull { it.getProviderType() == id }
        return p ?: providers.getValue(LocalUserProvider.PROVIDER_NAME)
    }
}
