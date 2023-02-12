import type Identity from "@/entities/identity";
import type { User } from "@/entities/user";

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

export interface WorkspaceResponse {
    code: number,
    data: WorkspaceWithUser[]
}

export interface WorkspacesResponseHanlder {
    success: (workspaces: WorkspaceWithUser[]) => any
    fail: (status: number, reason: string, data?: any) => any
}

export class UserAuthApi {
    constructor() {
        //
    }

    async userWorksapces(user: Identity, handler?: WorkspacesResponseHanlder) {

    }
}

const workspaceApiHelper: UserAuthApi = new UserAuthApi()
export default workspaceApiHelper
