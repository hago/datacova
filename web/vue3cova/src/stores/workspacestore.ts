import type { WorkspaceWithUser } from "@/api/workspaceapi";
import { defineStore } from "pinia";

type WorkspaceCache = {
    workspaces: Map<number, WorkspaceWithUser>
}

export const workspaceStore = defineStore("workspaces", {
    state: (): WorkspaceCache => {
        return {
            workspaces: new Map()
        }
    },
    actions: {
        getWorkspace(id: number): WorkspaceWithUser | null {
            let w = this.workspaces.get(id)
            return w === undefined ? null : w
        },
        setWorkspace(workspace: WorkspaceWithUser) {
            this.workspaces.set(workspace.workspace.id, workspace)
        }
    }
})
