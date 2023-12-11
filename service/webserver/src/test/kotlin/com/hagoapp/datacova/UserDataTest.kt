/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova

import com.hagoapp.datacova.data.user.UserData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserDataTest {

    @Test
    fun testComputePwdHash() {
        val pwd = "123456"
        val hash = "c8b90ad0de2dc8c6996315296b2718434d677df4cd37688a2a6f86c71c462e6a"
        Assertions.assertEquals(hash, UserData.computePwdHash(pwd))
    }
}
