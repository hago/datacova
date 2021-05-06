/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.user

class UserPrivacyFilter {
    companion object {
        const val HIDE_USERINFO_ID = 0x1
        const val HIDE_USERINFO_NAME = 0x2
        const val HIDE_USERINFO_DESCRIPTION = 0x4
        const val HIDE_USERINFO_ADDBY = 0x8
        const val HIDE_USERINFO_ADDTIME = 0x10
        const val HIDE_USERINFO_MODIFYBY = 0x20
        const val HIDE_USERINFO_MODIFYTIME = 0x40
        const val HIDE_USERINFO_STATUS = 0x80
        const val HIDE_USERINFO_THUMBNAIL = 0x100

        @JvmStatic
        fun maskUserInfo(input: UserInfo?, mask: Int = 0, maskString: String = "********"): UserInfo? {
            if (input == null) {
                return null
            }
            val newOne = UserInfo(input.userId, input.provider)
            newOne.pwdHash = maskString
            newOne.id = if (mask.and(HIDE_USERINFO_ID) == 0) input.id else -1
            newOne.description = if (mask.and(HIDE_USERINFO_DESCRIPTION) == 0) input.description else maskString
            newOne.name = if (mask.and(HIDE_USERINFO_NAME) == 0) input.name else maskString
            newOne.addBy = if (mask.and(HIDE_USERINFO_ADDBY) == 0) input.addBy else 0
            newOne.addTime = if (mask.and(HIDE_USERINFO_ADDTIME) == 0) input.addTime else -1L
            newOne.modifyBy = if (mask.and(HIDE_USERINFO_MODIFYBY) == 0) input.modifyBy else 0
            newOne.modifyTime = if (mask.and(HIDE_USERINFO_MODIFYTIME) == 0) input.modifyTime else 0
            newOne.status = if (mask.and(HIDE_USERINFO_STATUS) == 0) input.status else UserStatus.Unknown
            newOne.thumbnail = if (mask.and(HIDE_USERINFO_THUMBNAIL) == 0) input.thumbnail else null
            return newOne
        }

        @JvmStatic
        fun maskBatchUserInfo(input: List<UserInfo?>, mask: Int = 0, maskString: String = "********"): List<UserInfo?> {
            return input.map { maskUserInfo(it, mask, maskString) }
        }
    }
}
