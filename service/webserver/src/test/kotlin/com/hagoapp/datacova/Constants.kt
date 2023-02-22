/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova

class Constants {
    companion object {
        const val S3_ACCESS_KEY = "dc.s3.credential.key"
        const val S3_ACCESS_SECRET = "dc.s3.credential.secret"
        const val S3_ACCESS_TOKEN = "dc.s3.credential.token"
        const val S3_BUCKET_NAME = "dc.s3.bucket"
        const val S3_OBJECT_NAME = "dc.s3.obj.name"
        const val S3_SOURCE_NAME = "dc.s3.src"
        const val S3_REGION = "dc.s3.region"

        const val BLOB_CONTAINER_NAME = "dc.azure.store.container"
        const val BLOB_NAME = "dc.azure.store.blob"
        const val BLOB_SAS_URL = "dc.azure.store.blobsasurl"
    }
}
