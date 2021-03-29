/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.entity.action.ingest;

import com.hagoapp.f2t.F2TConfig;

public class IngestOptions extends F2TConfig {
    public IngestOptions() {
        super();
        batchColumnName = "BatchId";
    }
}
