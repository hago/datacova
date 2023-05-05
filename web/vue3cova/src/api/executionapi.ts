import type Identity from "@/entities/identity"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"
import type { TaskExecution } from "@/entities/execution/taskexecution"
import { addJsonRequestHeader, addTokenHeader } from "./credential"

export interface ExecutionListResponse extends BaseResponse {
  data: {
    executions: TaskExecution[]
    count: number
    start: number
  }
}

export interface ExecutionResponse extends BaseResponse {
  data: TaskExecution | null
}

export class ExecutionApi {
  constructor() {
    //
  }

  async getExecutions(user: Identity, workspaceId: number, start: number = 0, size: number = 10): Promise<ExecutionListResponse> {
    let headers = addJsonRequestHeader(addTokenHeader(user))
    let p = await fetch(`/api/workspace/${workspaceId}/executions/${start}/${size}`, {
      headers: headers,
      method: "GET"
    })
    return fromFetchResponse(p)
  }

  async getExecution(user: Identity, executionId: number): Promise<ExecutionResponse> {
    let headers = addJsonRequestHeader(addTokenHeader(user))
    let p = await fetch(`/api/execution/${executionId}`, {
      headers: headers,
      method: "GET"
    })
    return fromFetchResponse(p)
  }
}

const executionApiHelper = new ExecutionApi()
export default executionApiHelper
