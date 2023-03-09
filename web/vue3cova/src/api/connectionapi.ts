import type { BaseDbConfig } from "@/entities/connection/dbconfigbase"
import type { WorkspaceConnection } from "@/entities/connection/workspaceconnection"
import type Identity from "@/entities/identity"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"
import { addTokenHeader } from "./credential"

interface ConnectionsResponse extends BaseResponse {
    data: {
        owner: boolean
        canDelete: number[]
        canModify: number[]
        connections: WorkspaceConnection[]
    }
}

interface TableListResponse extends Response {
    data: {
        [key: string]: {
            schema: string,
            tableName: string
        }[]
    }
}

interface VerifyConnectionResponse extends Response {
    data: {
        result: boolean
        message?: string
        databases: string[]
    }
}

export class ConnectionApi {
    constructor() {
        //
    }

    async workspaceConnections(user: Identity, workspaceId: number): Promise<ConnectionsResponse> {
        let headers = addTokenHeader(user)
        let rsp = await fetch(`/api/workspace/${workspaceId}/connections`, {
            headers: headers,
            method: "GET"
        })
        return fromFetchResponse(rsp)
    }

    async listTables(user: Identity, workspaceId: number, connectionId: number): Promise<TableListResponse> {
        let headers = addTokenHeader(user)
        let rsp = await fetch(`/api/workspace/${workspaceId}/connection/${connectionId}/tables`, {
            headers: headers,
            method: "GET"
        })
        return fromFetchResponse(rsp)
    }

    async verifyConnection(user: Identity, config: BaseDbConfig): Promise<VerifyConnectionResponse> {
        let headers = addTokenHeader(user)
        headers.append('content-type', 'application/json')
        let rsp = await fetch(`/api/connection/verify`, {
            headers: headers,
            method: "POST",
            body: JSON.stringify(config)
        })
        return fromFetchResponse(rsp)
    }
}

const connApiHelper: ConnectionApi = new ConnectionApi()
export default connApiHelper
