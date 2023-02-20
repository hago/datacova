import type Identity from "@/entities/identity"
import type { Task } from "@/entities/task/task"
import type { BaseResponse } from "./basehandler"
import type BaseResponseHandler from "./basehandler"
import { addTokenHeader } from "./credential"
import type FailResponse from "./failresponse"

export interface TasksResponse {
    code: number
    data: {
        tasks: Task[]
    }
}

export interface TaskResponseHandler extends BaseResponseHandler {
    success: (rsp: TasksResponse) => any
}

export class TaskApi {
    constructor() {
        //
    }

    async getTasksOfWorkspace(user: Identity, workspaceId: number, handler?: TaskResponseHandler) {
        let headers = addTokenHeader(user)
        let p = fetch(`/api/workspace/${workspaceId}/tasks`, {
            headers: headers,
            method: "GET"
        })
        if (handler === undefined) {
            return p
        } else {
            p.then(rsp => {
                rsp.text().then(s => {
                    if (rsp.status === 200) {
                        handler.success(JSON.parse(s))
                    } else {
                        let rsperr = JSON.parse(s) as FailResponse
                        handler.fail(rsperr.code, rsperr.error.message, rsperr.error)
                    }
                })
            }).catch(err => {
                console.log("login fail: ", err)
                handler.fail(-1, "fetch error", err)
            })
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
            let rsperr = JSON.parse(s) as FailResponse
            throw new Error(`error: ${rsperr.error.message}, data: ${rsperr.error.data}`)
        }
    }
}

const taskApiHelper: TaskApi = new TaskApi()
export default taskApiHelper
