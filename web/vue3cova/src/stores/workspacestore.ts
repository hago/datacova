import type { WorkspaceWithUser } from "@/api/workspaceapi";
import { defineStore } from "pinia";

type WorkspaceCache = {
    workspaces: WorkspaceWithUser[],
    selectedWorkspace: WorkspaceWithUser | null
}

export const workspaceStore = defineStore("workspaces", {
    state: (): WorkspaceCache => {
        return {
            workspaces: [],
            selectedWorkspace: null
        }
    },
    actions: {
        getWorkspace(id: number): WorkspaceWithUser | null {
            let w = this.workspaces[id]
            return w === undefined ? null : w
        },
        setWorkspace(workspace: WorkspaceWithUser) {
            this.workspaces[workspace.workspace.id] = workspace
            console.log(`size: ${this.workspaces.length}`)
        }
    },
    persist: {
        storage: sessionStorage
    }
})
