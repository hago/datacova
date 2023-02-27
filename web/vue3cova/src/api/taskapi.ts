import type Identity from "@/entities/identity"
import type { Task } from "@/entities/task/task"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"
import { addTokenHeader } from "./credential"

export interface TasksResponse extends BaseResponse {
    data: {
        tasks: Task[]
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
}

const taskApiHelper: TaskApi = new TaskApi()
export default taskApiHelper
