/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.execution.datafile;

public enum DataFileType {
    Unknown,
    CSV,
    Excel,
    ExcelOpenXML;

    public static DataFileType getFromExtension(String ext) {
        switch (ext.trim().toLowerCase()) {
            case "csv":
                return CSV;
            case "xls":
                return Excel;
            case "xlsx":
                return ExcelOpenXML;
            default:
                return Unknown;
        }
    }
}
