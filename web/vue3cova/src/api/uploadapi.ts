import type { BaseFileInfo } from "@/entities/datafile/BaseFileInfo"
import type { CsvFileInfo } from "@/entities/datafile/CsvFileInfo"
import type { ExcelInfo } from "@/entities/datafile/ExcelInfo"
import type Identity from "@/entities/identity"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"
import { addJsonRequestHeader, addTokenHeader } from "./credential"
import type { TaskExecution } from "@/entities/execution/taskexecution"

export interface UploadedFileInfo {
    originalName: string
    id: string
    type: number
    extra: null | CsvFileInfo | BaseFileInfo | ExcelInfo
}

export interface UploadResponse extends BaseResponse {
    data: UploadedFileInfo[]
}

export interface PreviewResponse extends BaseResponse {
    data: {
        columns: string[]
        data: (string | null)[][]
    }
}

export interface ExecuteResponse extends BaseResponse {
    data: TaskExecution
}

export class UploadApi {
    constructor() {
        //
    }

    async uploadFile(user: Identity, file: File): Promise<UploadResponse> {
        let headers = addTokenHeader(user)
        let form = new FormData()
        form.append('file', file, file.name)
        let rsp = await fetch(`/api/file/upload`, {
            headers: headers,
            method: "PUT",
            body: form
        })
        return fromFetchResponse(rsp)
    }

    async previewFile(user: Identity, fileId: string, fileInfo: Object | undefined | null, rowCount = 20)
        : Promise<PreviewResponse> {
        let headers = addJsonRequestHeader(addTokenHeader(user))
        let rsp = await fetch(`/api/file/preview/${fileId}/${rowCount}`, {
            headers: headers,
            method: "POST",
            body: JSON.stringify(fileInfo)
        })
        return fromFetchResponse(rsp)
    }

    async executeTaskFile(user: Identity, workspaceId: number, taskId: number, fileId: string,
        fileInfo: Object | undefined | null): Promise<ExecuteResponse> {
        let headers = addJsonRequestHeader(addTokenHeader(user))
        let rsp = await fetch(`/api/file/execute/${fileId}/task/${workspaceId}/${taskId}`, {
            headers: headers,
            method: "POST",
            body: JSON.stringify(fileInfo)
        })
        return fromFetchResponse(rsp)
    }
}

const uploadApiHelper: UploadApi = new UploadApi()
export default uploadApiHelper
