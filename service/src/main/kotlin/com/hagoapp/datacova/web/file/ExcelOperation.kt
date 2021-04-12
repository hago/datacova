/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.web.file

import com.hagoapp.datacova.CoVaLogger
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.f2t.datafile.excel.ExcelDataFileParser
import com.hagoapp.f2t.datafile.excel.ExcelDataFileReader
import com.hagoapp.f2t.datafile.excel.FileInfoExcel
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext

class ExcelOperation {
    private val logger = CoVaLogger.getLogger()

    @WebEndPoint(
        methods = [HttpMethod.POST],
        path = "/api/excel/parse",
        authTypes = [AuthType.Anonymous]
    )
    fun parseExcel(context: RoutingContext) {
        val files = context.fileUploads()
        if (files.isEmpty()) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "no file found")
            return
        }
        val file = context.fileUploads().first()
        val info = ExcelDataFileParser(file.uploadedFileName()).getInfo()
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to info
            )
        )
    }
}
