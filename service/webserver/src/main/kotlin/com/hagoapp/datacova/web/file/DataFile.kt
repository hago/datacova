package com.hagoapp.datacova.web.file

import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.file.localfs.LocalFsFileStore
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.f2t.datafile.FileInfo
import com.hagoapp.f2t.datafile.csv.FileInfoCsv
import com.hagoapp.f2t.datafile.excel.ExcelDataFileParser
import com.hagoapp.f2t.datafile.excel.ExcelInfo
import com.hagoapp.f2t.datafile.excel.FileInfoExcel
import com.hagoapp.f2t.datafile.excel.FileInfoExcelX
import com.hagoapp.f2t.datafile.parquet.FileInfoParquet
import com.hagoapp.util.EncodingUtils
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import java.io.FileInputStream
import java.io.InputStream
import java.nio.charset.Charset

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
            val t = UploadedFileInfo.typeFromFileName(upload.fileName())
            fs.getFile(id).use { stream ->
                UploadedFileInfo(upload.fileName(), id, t, getExtra(t, stream))
            }
        }
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to fileIdList
            )
        )
    }

    data class UploadedFileInfo(
        val originalName: String,
        val id: String,
        val type: Int,
        val extra: Any? = null
    ) {
        companion object {
            fun typeFromFileName(fileName: String): Int {
                return when {
                    fileName.endsWith(".csv", true) -> FileInfoCsv.FILE_TYPE_CSV
                    fileName.endsWith(".xls", true) -> FileInfoExcel.FILE_TYPE_EXCEL
                    fileName.endsWith(".xlsx", true) -> FileInfoExcelX.FILE_TYPE_EXCEL_OPEN_XML
                    else -> FileInfo.FILE_TYPE_UNDETERMINED
                }
            }
        }
    }

    private fun getExtra(type: Int, stream: InputStream): Any? {
        return when (type) {
            FileInfoCsv.FILE_TYPE_CSV -> csvInfo(stream)
            FileInfoExcel.FILE_TYPE_EXCEL -> excelInfo(stream)
            FileInfoExcelX.FILE_TYPE_EXCEL_OPEN_XML -> excelXInfo(stream)
            FileInfoParquet.FILE_TYPE_PARQUET -> FileInfo()
            else -> null
        }
    }

    private fun csvInfo(stream: InputStream): FileInfoCsv {
        val fileInfo = FileInfoCsv()
        fileInfo.encoding = Charset.forName(EncodingUtils.guessEncoding(stream)).name()
        return fileInfo
    }

    private fun excelInfo(stream: InputStream): ExcelInfo {
        return ExcelDataFileParser(stream).getInfo()
    }

    private fun excelXInfo(stream: InputStream): ExcelInfo {
        return excelInfo(stream)
    }
}
