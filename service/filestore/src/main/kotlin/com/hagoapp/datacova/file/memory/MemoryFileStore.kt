/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.file.memory

import com.hagoapp.datacova.file.FileStore
import com.hagoapp.datacova.file.FsConfig
import com.hagoapp.datacova.file.FsScheme
import com.hagoapp.datacova.file.StoreFileInfo
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

@FsScheme(name = MemFsConfig.MEMORY_FS_SCHEME)
class MemoryFileStore(private val config: FsConfig) : FileStore {

    private val storage = ArrayList<ByteArray?>(16)
    private val infoMap = mutableMapOf<String, Pair<StoreFileInfo, Int>>()

    override fun putFile(src: InputStream, fileName: String, size: Long): String {
        val id = UUID.randomUUID().toString()
        val bytes = ByteArrayOutputStream().use {
            val buffer = ByteArray(1024 * 1024)
            while (true) {
                val i = src.read(buffer, 0, buffer.size)
                if (i < 0) {
                    break
                }
                it.write(buffer, 0, i)
            }
            it.toByteArray()
        }
        var slot = -1
        for (i in storage.indices) {
            if (storage[i] == null) {
                storage[i] = bytes
                slot = i
                break
            }
        }
        if (slot < 0) {
            storage.add(bytes)
            slot = storage.size - 1
        }
        val info = StoreFileInfo(fileName, id, size)
        infoMap[id] = Pair(info, slot)
        return id
    }

    override fun getFile(id: String): InputStream {
        val p = infoMap[id] ?: throw UnsupportedOperationException("Not found $id")
        return ByteArrayInputStream(storage[p.second])
    }

    override fun getFileInfo(id: String): StoreFileInfo {
        return infoMap[id]?.first ?: StoreFileInfo(null, null, null)
    }

    override fun delete(id: String): Boolean {
        val p = infoMap[id] ?: return false
        storage[p.second] = null
        infoMap.remove(id)
        return true
    }

    override fun exists(id: String): Boolean {
        return infoMap.containsKey(id)
    }
}
