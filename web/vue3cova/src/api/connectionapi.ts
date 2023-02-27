import type { WorkspaceConnection } from "@/entities/connection/workspaceconnection"
import type Identity from "@/entities/identity"
import { fromFetchResponse, type BaseResponse } from "./baseresponse"
import { addTokenHeader } from "./credential"

interface ConnectionsResponse extends BaseResponse {
    data: {
        owner: boolean
        canDelete: boolean
        canModify: boolean
        connections: WorkspaceConnection[]
    }
}

interface TableListResponse extends Response {
    data: Map<string, {
        schema: string,
        tableName: string
    }[]>
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
        let rsp = await fetch(`/api/workspace/${workspaceId}/connections`, {
            headers: headers,
            method: "GET"
        })
        let s = await rsp.text()
        return fromFetchResponse(rsp)
    }
}

const connApiHelper: ConnectionApi = new ConnectionApi()
export default connApiHelper
