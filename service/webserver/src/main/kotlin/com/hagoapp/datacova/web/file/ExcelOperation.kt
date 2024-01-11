/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.file

import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.f2t.datafile.excel.ExcelDataFileParser
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory

class ExcelOperation {
    private val logger = LoggerFactory.getLogger(ExcelOperation::class.java)

    @WebEndPoint(
        methods = [MethodName.POST],
        path = "/api/excel/parse",
        authTypes = [AuthType.ANONYMOUS]
    )
    fun parseExcel(context: RoutingContext) {
        val files = context.fileUploads()
        if (files.isEmpty()) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "no file found")
            return
        }
        val file = context.fileUploads().first()
        val info = ExcelDataFileParser(file.uploadedFileName()).excelInfo()
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to info
            )
        )
    }
}
