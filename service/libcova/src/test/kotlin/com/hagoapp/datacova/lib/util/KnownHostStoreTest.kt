/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.lib.util

import com.hagoapp.datacova.lib.ssh.HostKeyItem
import com.hagoapp.datacova.lib.util.ssh.KnownHostsStore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class KnownHostStoreTest {

    private val item = HostKeyItem("sample.demo.com", "ssh-rsa", "abcdefghijklmnopqrstuvwxyz")

    @Test
    fun testMemoryStore() {
        val k = KnownHostsStore.MemoryKnownHostStore()
        k.update(item)
        val i = k.findHostKetItem(item.host)
        Assertions.assertEquals(i, item)
    }
}