import type Identity from "@/entities/identity";
import type { User } from "@/entities/user";
import type { BaseResponse } from "./baseresponse";
import { addTokenHeader } from "./credential";
import { stringifyFailResponseBody } from "./failresponse";

export interface Workspace {
    id: number
    name: string
    description: string
    ownerId: string
}

export enum WorkSpaceUserRole {
    Admin = 0,
    Maintainer = 1,
    Loader = 2
}

export interface WorkspaceUser {
    user: User
    roles: WorkSpaceUserRole[]
}

export interface WorkspaceWithUser {
    workspace: Workspace
    owner: User
    users: WorkspaceUser[]
}

export interface WorkspaceListResponse extends BaseResponse {
    data: WorkspaceWithUser[]
}

export interface WorkspaceResponse extends BaseResponse {
    data: WorkspaceWithUser
}

export class WorkspaceApi {
    constructor() {
        //
    }

    async userWorksapces(user: Identity): Promise<WorkspaceListResponse> {
        let headers = addTokenHeader(user)
        let p = await fetch("/api/workspaces/mine", {
            headers: headers,
            method: "GET"
        })
        let s = await p.text()
        if (p.status === 200) {
            let rsp: WorkspaceListResponse = JSON.parse(s)
            return Promise.resolve(rsp)
        } else {
            throw new Error(stringifyFailResponseBody(s))
        }
    }

    async getWorksapce(user: Identity, id: number): Promise<WorkspaceResponse> {
        let headers = addTokenHeader(user)
        let p = await fetch(`/api/workspace/${id}`, {
            headers: headers,
            method: "GET"
        })
        let s = await p.text()
        if (p.status === 200) {
            let rsp: WorkspaceResponse = JSON.parse(s)
            return Promise.resolve(rsp)
        } else {
            throw new Error(stringifyFailResponseBody(s))
        }
    }
}

const workspaceApiHelper: WorkspaceApi = new WorkspaceApi()
export default workspaceApiHelper
