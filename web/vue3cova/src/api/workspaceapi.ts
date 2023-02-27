import type Identity from "@/entities/identity";
import type { User } from "@/entities/user";
import { fromFetchResponse, type BaseResponse } from "./baseresponse";
import { addTokenHeader } from "./credential";

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
        return fromFetchResponse(p)
    }

    async getWorksapce(user: Identity, id: number): Promise<WorkspaceResponse> {
        let headers = addTokenHeader(user)
        let p = await fetch(`/api/workspace/${id}`, {
            headers: headers,
            method: "GET"
        })
        return fromFetchResponse(p)
    }
}

const workspaceApiHelper: WorkspaceApi = new WorkspaceApi()
export default workspaceApiHelper
