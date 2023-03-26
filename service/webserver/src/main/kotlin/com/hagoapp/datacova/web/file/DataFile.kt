package com.hagoapp.datacova.web.file

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.file.localfs.LocalFsFileStore
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import java.io.FileInputStream

class DataFile {
    @WebEndPoint(
        methods = [MethodName.PUT],
        path = "/api/file/upload",
        authTypes = [AuthType.UserToken]
    )
    fun uploadFile(context: RoutingContext) {
        if (context.fileUploads().isEmpty()) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "No file")
            return
        }
        val fs = LocalFsFileStore.getFileStore(CoVaConfig.getConfig().fileStorage.uploadDirectory)
        val fileIdList = context.fileUploads().map { upload ->
            val id = FileInputStream(upload.uploadedFileName()).use { fis ->
                fs.putFile(fis, upload.fileName(), upload.size())
            }
            Pair(upload.fileName(), id)
        }
        ResponseHelper.sendResponse(context, HttpResponseStatus.OK, mapOf(
            "code" to 0,
            "data" to fileIdList
        ))
    }
}
