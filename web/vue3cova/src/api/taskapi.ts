import type Identity from "@/entities/identity"
import type { Task } from "@/entities/task/task"
import type { BaseResponse } from "./baseresponse"
import { addTokenHeader } from "./credential"
import { stringifyFailResponseBody } from "./failresponse"

export interface TasksResponse {
    code: number
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
        let s = await rsp.text()
        if (rsp.status === 200) {
            let tasks = JSON.parse(s) as TasksResponse
            return Promise.resolve(tasks)
        } else {
            throw new Error(stringifyFailResponseBody(s))
        }
    }

    async deleteTask(user: Identity, task: Task): Promise<number> {
        let headers = addTokenHeader(user)
        let rsp = await fetch(`/api/workspace/${task.workspaceId}/task/${task.id}`, {
            headers: headers,
            method: "DELETE"
        })
        let s = await rsp.text()
        if (rsp.status === 200) {
            let r: BaseResponse = JSON.parse(s)
            return Promise.resolve(r.code)
        } else {
            throw new Error(stringifyFailResponseBody(s))
        }
    }
}

const taskApiHelper: TaskApi = new TaskApi()
export default taskApiHelper
