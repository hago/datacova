/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.execution.distribute

import com.hagoapp.datacova.CoVaException
import com.hagoapp.datacova.distribute.Distributor
import com.hagoapp.datacova.distribute.TaskActionDistribute
import com.hagoapp.datacova.distribute.conf.AzureBlobConfig

class AzureBlobDistributor: Distributor() {

    private lateinit var config: AzureBlobConfig

    override fun init(action: TaskActionDistribute?) {
        super.init(action)
        if ((action == null) || action.configuration !is AzureBlobConfig) {
            throw CoVaException("null distribute action!")
        }
        config = action.configuration as AzureBlobConfig
    }

    override fun distribute(source: String) {
        TODO()
    }

    override fun supportedDistributionType(): String {
        return AzureBlobConfig.DISTRIBUTION_TYPE_AZURE_BLOB
    }
}
