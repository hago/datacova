/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.file

import com.hagoapp.datacova.file.localfs.LocalFsConfig
import com.hagoapp.datacova.file.localfs.LocalFsFileStore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FactoryTest {

    @Test
    fun testLocalFsConfig() {
        val config = LocalFsConfig()
        Assertions.assertNotNull(config.rootPath)
        Assertions.assertFalse(config.rootPath.isBlank())
        val fileStore = FileStoreFactory.createFileStore(config)
        Assertions.assertNotNull(fileStore)
        Assertions.assertEquals(LocalFsFileStore::class.java, fileStore.javaClass)
    }

    @Test
    fun testConnectionStringWithoutScheme() {
        val cases = listOf("c:/temp", "/usr/local/tmp")
        for (c in cases) {
            val f = FileStoreFactory.createFileStore(c)
            Assertions.assertNull(f)
        }
    }
}
