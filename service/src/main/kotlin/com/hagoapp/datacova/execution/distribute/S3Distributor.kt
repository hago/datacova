/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.distribute

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.BasicSessionCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.distribute.Distributor
import com.hagoapp.datacova.entity.action.distribute.conf.S3Config
import java.io.File

class S3Distributor : Distributor() {

    class LocalCredentialProvider(private val config: S3Config) : AWSCredentialsProvider {

        private val credential: AWSCredentials = if (config.credential.token != null)
            BasicAWSCredentials(config.credential.accessKey, config.credential.secret)
        else
            BasicSessionCredentials(config.credential.accessKey, config.credential.secret, config.credential.token)

        override fun getCredentials(): AWSCredentials {
            return credential
        }

        override fun refresh() {
            // no need
        }

    }

    override fun distribute(source: String?) {
        if (distAction.configuration !is S3Config) {
            throw UnsupportedOperationException("Not an S3 distributor config")
        }
        val config: S3Config = distAction.configuration as S3Config
        val builder = AmazonS3ClientBuilder.standard()
            .withCredentials(buildCredentialProvider(config))
        if (config.region != null) {
            val region = try {
                val r = Regions.fromName(config.region)
                logger.debug("region {} is applied", config.region)
                r
            } catch (e: IllegalArgumentException) {
                logger.warn("region {} is not valid, failed to defult", config.region)
                Regions.DEFAULT_REGION
            }
            builder.withRegion(region)
        }
        val s3 = builder.build()
        if (!s3.doesBucketExistV2(config.bucket)) {
            if (config.isCreateBucketIfAbsent) {
                s3.createBucket(config.bucket)
                logger.debug("bucket {} found", config.bucket)
            } else {
                logger.error("bucket {} not found", config.bucket)
                throw CoVaException("S3 bucket ${config.bucket} not found and auto creation is not enabled.")
            }
        }
        source ?: throw UnsupportedOperationException("null file to distribute")
        if (!config.isOverwriteExisted && s3.doesObjectExist(config.bucket, config.targetFileName)) {
            logger.error("file {} existed in bucket {}, overwrite is forbidden")
            throw CoVaException("file ${config.targetFileName} existed in bucket ${config.bucket}, overwrite is forbidden")
        }
        s3.putObject(config.bucket, config.targetFileName, File(source))
    }

    private fun buildCredentialProvider(config: S3Config): AWSCredentialsProvider {
        return LocalCredentialProvider(config)
    }

    override fun supportedDistributionType(): String {
        return S3Config.DISTRIBUTION_TYPE_S3;
    }
}