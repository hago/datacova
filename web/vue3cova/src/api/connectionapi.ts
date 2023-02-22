import type { WorkspaceConnection } from "@/entities/connection/workspaceconnection"
import type Identity from "@/entities/identity"
import type { BaseResponse } from "./baseresponse"
import { addTokenHeader } from "./credential"
import { stringifyFailResponseBody } from "./failresponse"

interface ConnectionsResponse extends BaseResponse {
    data: {
        owner: boolean
        canDelete: boolean
        canModify: boolean
        connections: WorkspaceConnection[]
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
        let s = await rsp.text()
        if (rsp.status === 200) {
            let connections = JSON.parse(s) as ConnectionsResponse
            return Promise.resolve(connections)
        } else {
            throw new Error(stringifyFailResponseBody(s))
        }
    }

}

const connApiHelper: ConnectionApi = new ConnectionApi()
export default connApiHelper
