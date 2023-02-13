import type Identity from "@/entities/identity";
import type { User } from "@/entities/user";
import { addTokenHeader } from "./credential";
import type FailResponse from "./failresponse";

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

export interface WorkspaceListResponse {
    code: number,
    data: WorkspaceWithUser[]
}

export interface WorkspacesListResponseHanlder {
    success: (response: WorkspaceListResponse) => any
    fail: (status: number, reason: string, data?: any) => any
}

export interface WorkspaceResponse {
    code: number,
    data: WorkspaceWithUser
}

export interface WorkspaceResponseHanlder {
    success: (response: WorkspaceResponse) => any
    fail: (status: number, reason: string, data?: any) => any
}

export class WorkspaceApi {
    constructor() {
        //
    }

    async userWorksapces(user: Identity, handler?: WorkspacesListResponseHanlder) {
        let headers = addTokenHeader(user)
        let p = fetch("/api/workspaces/mine", {
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

    async getWorksapce(user: Identity, id: number, handler?: WorkspaceResponseHanlder) {
        let headers = addTokenHeader(user)
        let p = fetch(`/api/workspace/${id}`, {
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
}

const workspaceApiHelper: WorkspaceApi = new WorkspaceApi()
export default workspaceApiHelper
