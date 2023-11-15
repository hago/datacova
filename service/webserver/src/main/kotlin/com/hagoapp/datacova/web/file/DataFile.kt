package com.hagoapp.datacova.web.file

import com.hagoapp.datacova.utility.Utils
import com.hagoapp.datacova.config.CoVaConfig
import com.hagoapp.datacova.config.FileStorageConfig
import com.hagoapp.datacova.data.execution.TaskExecutionCache
import com.hagoapp.datacova.lib.data.TaskExecutionData
import com.hagoapp.datacova.data.workspace.TaskCache
import com.hagoapp.datacova.data.workspace.WorkspaceCache
import com.hagoapp.datacova.lib.execution.ExecutionFileInfo
import com.hagoapp.datacova.lib.execution.TaskExecution
import com.hagoapp.datacova.util.WorkspaceUserRoleUtil
import com.hagoapp.datacova.util.http.ResponseHelper
import com.hagoapp.datacova.web.MethodName
import com.hagoapp.datacova.web.annotation.WebEndPoint
import com.hagoapp.datacova.web.authentication.AuthType
import com.hagoapp.datacova.web.authentication.Authenticator
import com.hagoapp.f2t.datafile.FileInfo
import com.hagoapp.f2t.datafile.FileInfoReader
import com.hagoapp.f2t.datafile.ReaderFactory
import com.hagoapp.f2t.datafile.csv.FileInfoCsv
import com.hagoapp.f2t.datafile.excel.ExcelDataFileParser
import com.hagoapp.f2t.datafile.excel.ExcelInfo
import com.hagoapp.f2t.datafile.excel.FileInfoExcel
import com.hagoapp.f2t.datafile.excel.FileInfoExcelX
import com.hagoapp.f2t.datafile.parquet.FileInfoParquet
import com.hagoapp.util.EncodingUtils
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.ext.web.RoutingContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.UUID

class DataFile {

    private val logger = LoggerFactory.getLogger(DataFile::class.java)

    @WebEndPoint(
        methods = [MethodName.PUT],
        path = "/api/file/upload",
        authTypes = [AuthType.USER_TOKEN]
    )
    fun uploadFile(context: RoutingContext) {
        if (context.fileUploads().isEmpty()) {
            ResponseHelper.respondError(context, HttpResponseStatus.BAD_REQUEST, "No file")
            return
        }
        val fs = FileStorageConfig.createFileStore(CoVaConfig.getConfig().fileStorage.uploadFileStore)
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
                    fileName.endsWith(".parquet", true) -> FileInfoParquet.FILE_TYPE_PARQUET
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

    @WebEndPoint(
        methods = [MethodName.POST],
        path = "/api/file/preview/:id/:count",
        authTypes = [AuthType.USER_TOKEN]
    )
    fun previewFile(context: RoutingContext) {
        val id = context.pathParam("id")
        val count = context.pathParam("count").toIntOrNull() ?: 20
        val fs = FileStorageConfig.createFileStore(CoVaConfig.getConfig().fileStorage.uploadFileStore)
        if (!fs.exists(id)) {
            ResponseHelper.sendResponse(context, HttpResponseStatus.NOT_FOUND)
            return
        }
        val info = fs.getFileInfo(id)
        val tmpFile = File(Utils.getSystemTemporaryDirectory(), UUID.randomUUID().toString() + info.originalFileName)
        fs.getFile(id).use { fis ->
            FileOutputStream(tmpFile).use { fos ->
                Utils.copyStream(fis, fos)
            }
        }
        val metaInfo = FileInfoReader.createFileInfo(
            context.body().asString(StandardCharsets.UTF_8.name()).toByteArray(StandardCharsets.UTF_8)
        )
        metaInfo.filename = tmpFile.absolutePath
        val columns: List<String>
        val data: List<List<String?>>
        ReaderFactory.getReader(metaInfo, false).use { reader ->
            reader.open(metaInfo)
            columns = reader.findColumns().map { it.name }
            data = mutableListOf()
            for (i in 0 until count) {
                val row = reader.next()
                data.add(row.cells.map { cell -> cell.data?.toString() })
            }
        }
        if (!tmpFile.delete()) {
            logger.warn("delete temp file {} failed", tmpFile.absolutePath)
        }
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to mapOf(
                    "columns" to columns,
                    "data" to data
                )
            )
        )
    }

    @WebEndPoint(
        methods = [MethodName.POST],
        path = "/api/file/execute/:id/task/:wkid/:taskid",
        authTypes = [AuthType.USER_TOKEN]
    )
    fun execute(context: RoutingContext) {
        val user = Authenticator.getUser(context)
        val workspaceId = context.pathParam("wkid").toInt()
        val workSpace = WorkspaceCache.getWorkspace(workspaceId)
        if ((workSpace == null) || !WorkspaceUserRoleUtil.isUser(user, workspaceId)) {
            ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "access denied")
            return
        }
        val id = context.pathParam("id")
        val taskId = context.pathParam("taskid").toInt()
        val fs = FileStorageConfig.createFileStore(CoVaConfig.getConfig().fileStorage.uploadFileStore)
        if (!fs.exists(id)) {
            ResponseHelper.sendResponse(context, HttpResponseStatus.NOT_FOUND)
            return
        }
        val info = fs.getFileInfo(id)
        val metaInfo = FileInfoReader.createFileInfo(
            context.body().asString(StandardCharsets.UTF_8.name()).toByteArray(StandardCharsets.UTF_8)
        )
        val exec = TaskExecutionData(CoVaConfig.getConfig().database).use { db ->
            val eai = ExecutionFileInfo()
            with(eai) {
                originalName = info.originalFileName
                size = info.size!!
                fileInfo = metaInfo
                fileId = info.id
            }
            val execTask = TaskExecution()
            val task = TaskCache.getTask(workspaceId, taskId)
            with(execTask) {
                this.taskId = taskId
                this.addBy = user.id
                fileInfo = eai
                this.task = task
            }
            db.createTaskExecution(execTask)
        }
        TaskExecutionCache.clearWorkspaceTaskExecutions(workspaceId)
        ResponseHelper.sendResponse(
            context, HttpResponseStatus.OK, mapOf(
                "code" to 0,
                "data" to exec
            )
        )
    }
}
