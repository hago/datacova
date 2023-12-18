import { WorkSpaceUserRole, type WorkspaceWithUser } from "@/api/workspaceapi";
import type Identity from "@/entities/identity";


export function isAdmin(user: Identity, workspace: WorkspaceWithUser): boolean {
    return (workspace.owner.userId === user.id) ||
        workspace.users.find(wu =>
            (wu.user.userId === user.id) && (wu.roles.indexOf(WorkSpaceUserRole.ADMIN) >= 0)) !== undefined
}

export function isMaintainer(user: Identity, workspace: WorkspaceWithUser): boolean {
    return workspace.users.find(wu =>
        (wu.user.userId === user.id) && (wu.roles.indexOf(WorkSpaceUserRole.MAINTAINER) >= 0)) !== undefined
}

export function isLoader(user: Identity, workspace: WorkspaceWithUser): boolean {
    return workspace.users.find(wu =>
        (wu.user.userId === user.id) && (wu.roles.indexOf(WorkSpaceUserRole.LOADER) >= 0)) !== undefined
}
