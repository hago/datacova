package com.hagoapp.datacova.user

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.data.UserData

class LocalUserProvider : UserAuthProvider {

    companion object {
        const val PROVIDER_NAME = "local"
    }

    private val dbConfig = CoVaConfig.getConfig().database

    override fun authenticate(userId: String, vararg credentials: String): Boolean {
        if (credentials.size != 1) {
            return false
        }
        UserData(dbConfig).use {
            return it.authenticate(userId, credentials[0])
        }
    }

    override fun getProviderName(): String {
        return PROVIDER_NAME
    }

    override fun getUserInfo(userId: String): UserInfo? {
        UserData(dbConfig).use { return it.findUser(userId) }
    }
}