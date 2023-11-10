/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.filestore

import com.hagoapp.datacova.file.FileStore
import com.hagoapp.datacova.file.FsConfig
import com.hagoapp.datacova.file.FsScheme
import com.hagoapp.datacova.file.StoreFileInfo
import com.hagoapp.datacova.utility.redis.JedisManager
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

@FsScheme(name = RedisFsConfig.REDIS_FILE_STORE)
class RedisFileStore(private val config: FsConfig) : FileStore {

    companion object {
        private const val FILE_NAMES_KEY = "redis.fs.name.index"
        private const val FILE_SIZES_KEY = "redis.fs.size.index"
    }

    private val logger = LoggerFactory.getLogger(RedisFileStore::class.java)
    private val fsConfig: RedisFsConfig

    init {
        if (config!is RedisFsConfig) {
            throw UnsupportedOperationException("Not a RedisFsConfig")
        }
        fsConfig = config
    }

    override fun putFile(src: InputStream, fileName: String, size: Long): String {
        JedisManager.getJedis(fsConfig.config).use { jedis ->
            val id = UUID.randomUUID().toString()
            jedis.hset(FILE_NAMES_KEY, id, fileName)
            var s = 0L
            ByteArrayOutputStream().use {
                val buffer = ByteArray(10 * 1024 * 1024)
                while (true) {
                    val i = src.read(buffer, 0, buffer.size)
                    if (i < 0) {
                        break
                    }
                    s += i
                    it.write(buffer, 0, i)
                }
                if (s != size) {
                    logger.warn("actual size of file $fileName is $s, not expected $size")
                }
                jedis.hset(FILE_SIZES_KEY, id, size.toString())
                jedis.set(id.toByteArray(), it.toByteArray())
            }
            return id
        }
    }

    override fun getFile(id: String): InputStream {
        JedisManager.getJedis(fsConfig.config).use { jedis ->
            val bytes = jedis.get(id.toByteArray()) ?: throw UnsupportedOperationException("Not existed: $id")
            return ByteArrayInputStream(bytes)
        }
    }

    override fun getFileInfo(id: String): StoreFileInfo {
        JedisManager.getJedis(fsConfig.config).use { jedis ->
            val fn = jedis.hget(FILE_NAMES_KEY, id) ?: return StoreFileInfo(null, null, null)
            val size = jedis.hget(FILE_SIZES_KEY, id) ?: return StoreFileInfo(null, null, null)
            return StoreFileInfo(fn, id, size.toLong())
        }
    }

    override fun delete(id: String): Boolean {
        JedisManager.getJedis(fsConfig.config).use { jedis ->
            jedis.del(id.toByteArray())
            jedis.hdel(FILE_NAMES_KEY, id)
            jedis.hdel(FILE_SIZES_KEY, id)
            return true
        }
    }

    override fun exists(id: String): Boolean {
        JedisManager.getJedis(fsConfig.config).use { jedis ->
            val b1 = jedis.hexists(FILE_NAMES_KEY, id)
            val b2 = jedis.exists(id.toByteArray())
            return if (b1 && b2) {
                true
            } else {
                delete(id)
                false
            }
        }
    }
}
