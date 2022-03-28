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
import com.hagoapp.datacova.distribute.conf.AzureBlobConfig
import com.hagoapp.datacova.execution.distribute.AzureBlobDistributor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class BlobDistributorTest {
    companion object {
        private val dist = TaskActionDistribute()
        private val logger = CoVaLogger.getLogger()

        @JvmStatic
        @BeforeAll
        fun setup() {
            val config = AzureBlobConfig()
            //config.blobSasUrl = System.getProperty(Constants.BLOB_SAS_URL)
            config.blobSasUrl="https://hagoappblob.blob.core.windows.net/?sv=2020-08-04&ss=bfqt&srt=sco&sp=rwdlacupx&se=2022-03-25T13:28:51Z&st=2022-03-25T05:28:51Z&spr=https&sig=ya%2BJ01FleLiS6HiM%2FU4RcjnCZNNA1QGrjQDdYYzXBmg%3D"
            config.targetFileName = System.getProperty(Constants.BLOB_NAME)
            config.containerName = System.getProperty(Constants.BLOB_CONTAINER_NAME)
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
            val s3config = dist.configuration as AzureBlobConfig
            logger.info(
                "upload file {} to {} in azure blob {}, overwrite is {}, {} is expected",
                case.first, s3config.targetFileName, s3config.containerName,
                if (s3config.isOverwriteExisted) "enabled" else "disabled", case.third
            )
            dist.configuration.isOverwriteExisted = case.second
            val block = {
                val blob = AzureBlobDistributor()
                blob.init(dist)
                blob.distribute(case.first)
            }
            if (case.third) Assertions.assertDoesNotThrow(block)
            else Assertions.assertThrows(CoVaException::class.java, block)
        }
    }
}
