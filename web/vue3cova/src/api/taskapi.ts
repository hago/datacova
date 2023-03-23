import type { BaseFileInfo } from "@/entities/datafile/BaseFileInfo"
import type { CsvFileInfo } from "@/entities/datafile/CsvFileInfo"
import type Identity from "@/entities/identity"
import type { Task } from "@/entities/task/task"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"
import { addTokenHeader } from "./credential"

export interface TasksResponse extends BaseResponse {
    data: {
        tasks: Task[]
    }
}

export interface TaskResponse extends BaseResponse {
    data: Task
}

export interface PreviewResponse extends BaseResponse {
    data: {
        columns: string[]
        info: CsvFileInfo
        rows: (string | number)[][]
    }
}

export class TaskApi {
    constructor() {
        //
    }

    async getTasksOfWorkspace(user: Identity, workspaceId: number): Promise<TasksResponse> {
        let headers = addTokenHeader(user)
        let rsp = await fetch(`/api/workspace/${workspaceId}/tasks`, {
            headers: headers,
            method: "GET"
        })
        return fromFetchResponse(rsp)
    }

    async deleteTask(user: Identity, task: Task): Promise<number> {
        let headers = addTokenHeader(user)
        let rsp = await fetch(`/api/workspace/${task.workspaceId}/task/${task.id}`, {
            headers: headers,
            method: "DELETE"
        })
        return fromFetchResponse(rsp)
    }

    async saveTask(user: Identity, task: Task): Promise<TaskResponse> {
        let headers = addTokenHeader(user)
        headers.append('content-type', 'application/json')
        let rsp = await fetch(`/api/workspace/${task.workspaceId}/task/update`, {
            headers: headers,
            method: "PUT",
            body: JSON.stringify(task)
        })
        return fromFetchResponse(rsp)
    }

    async getTask(user: Identity, workspaceId: number, taskId: number): Promise<TaskResponse> {
        let headers = addTokenHeader(user)
        headers.append('content-type', 'application/json')
        let rsp = await fetch(`/api/workspace/${workspaceId}/task/${taskId}`, {
            headers: headers,
            method: "GET"
        })
        return fromFetchResponse(rsp)
    }

    async runTask(user: Identity, task: Task, file: File, extra: Object): Promise<TaskResponse> {
        let headers = addTokenHeader(user)
        let form = new FormData()
        form.append('file', file, file.name)
        form.append('extra', JSON.stringify(extra))
        let rsp = await fetch(`/api/workspace/${task.workspaceId}/task/${task.id}/run`, {
            headers: headers,
            method: "POST",
            body: JSON.stringify(task)
        })
        return fromFetchResponse(rsp)
    }

    async previewFile(user: Identity, file: File, extra: Object, start?: number, size?: number): Promise<PreviewResponse> {
        let headers = addTokenHeader(user)
        let form = new FormData()
        form.append('file', file, file.name)
        form.append('extra', JSON.stringify(extra))
        if (start !== undefined) {
            form.append('start', start.toString())
        }
        if (size !== undefined) {
            form.append('size', size.toString())
        }
        let rsp = await fetch(`/api/file/preview`, {
            headers: headers,
            method: "POST",
            body: form
        })
        return fromFetchResponse(rsp)
    }
}

const taskApiHelper: TaskApi = new TaskApi()
export default taskApiHelper
