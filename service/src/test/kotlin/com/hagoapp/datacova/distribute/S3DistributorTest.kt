/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.distribute

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.Constants
import com.hagoapp.datacova.distribute.conf.S3Config
import com.hagoapp.datacova.execution.distribute.S3Distributor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperties
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

@EnabledIfSystemProperties(
    EnabledIfSystemProperty(named = Constants.S3_ACCESS_KEY, matches = ".*"),
    EnabledIfSystemProperty(named = Constants.S3_ACCESS_SECRET, matches = ".*"),
    EnabledIfSystemProperty(named = Constants.S3_BUCKET_NAME, matches = ".*"),
    EnabledIfSystemProperty(named = Constants.S3_OBJECT_NAME, matches = ".*")
)
class S3DistributorTest {

    companion object {
        private val dist = TaskActionDistribute()
        private val logger = CoVaLogger.getLogger()

        @JvmStatic
        @BeforeAll
        fun setup() {
            val config = S3Config()
            config.credential = S3Config.Credential()
            config.credential.accessKey = System.getProperty(Constants.S3_ACCESS_KEY)
            config.credential.secret = System.getProperty(Constants.S3_ACCESS_SECRET)
            config.credential.token = System.getProperty(Constants.S3_ACCESS_TOKEN)
            config.bucket = System.getProperty(Constants.S3_BUCKET_NAME)
            config.region = System.getProperty(Constants.S3_REGION)
            config.targetFileName = System.getProperty(Constants.S3_OBJECT_NAME)
            println(config.toJson())

            dist.configuration = config
            dist.name = "S3DistTest"
        }
    }

    private val cases = listOf(
        Triple(getSourceFileName(), false, true),
        Triple(getSourceFileName(), false, false),
        Triple(getSourceFileName(), true, true)
    )

    private fun getSourceFileName(): String {
        val f = System.getProperty(Constants.S3_SOURCE_NAME)
        return f ?: "./gradlew.bat"
    }

    @Test
    fun writeTest() {
        for (case in cases) {
            val s3config = dist.configuration as S3Config
            logger.info(
                "upload file {} to {} in S3 bucket {}, overwrite is {}, {} is expected",
                case.first, s3config.targetFileName, s3config.bucket,
                if (s3config.isOverwriteExisted) "enabled" else "disabled", case.third
            )
            dist.configuration.isOverwriteExisted = case.second
            val block = {
                val s3 = S3Distributor()
                s3.init(dist)
                s3.distribute(case.first)
            }
            if (case.third) Assertions.assertDoesNotThrow(block)
            else Assertions.assertThrows(CoVaException::class.java, block)
        }
    }
}
