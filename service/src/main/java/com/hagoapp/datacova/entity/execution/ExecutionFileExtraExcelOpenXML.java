/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.entity.execution;

import com.hagoapp.datacova.execution.datafile.DataFileType;

public class ExecutionFileExtraExcelOpenXML extends ExecutionFileExtra {
    public ExecutionFileExtraExcelOpenXML() {
        super();
        type = DataFileType.ExcelOpenXML;
    }
}
