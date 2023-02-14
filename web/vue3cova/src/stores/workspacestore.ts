import type { WorkspaceWithUser } from "@/api/workspaceapi";
import { defineStore } from "pinia";

type WorkspaceCache = {
    workspaces: Map<number, WorkspaceWithUser>,
    selectedWorkspace: WorkspaceWithUser | null
}

export const workspaceStore = defineStore("workspaces", {
    state: (): WorkspaceCache => {
        return {
            workspaces: new Map(),
            selectedWorkspace: null
        }
    },
    actions: {
        getWorkspace(id: number): WorkspaceWithUser | null {
            let w = this.workspaces.get(id)
            return w === undefined ? null : w
        },
        setWorkspace(workspace: WorkspaceWithUser) {
            this.workspaces.set(workspace.workspace.id, workspace)
        },
        getSelectedWorkspace(): WorkspaceWithUser | null {
            return this.selectedWorkspace
        },
        selectWorkspace(w: WorkspaceWithUser) {
            this.selectedWorkspace = w
        },
        selectWorkspaceId(id: number) {
            let w = this.workspaces.get(id)
            if (w == undefined) {
                console.log(`error: workspace with id ${id} not found in store`)
                this.selectedWorkspace = null
            } else {
                this.selectedWorkspace = w
            }
        },
        clearSelectedWorkspace() {
            this.selectedWorkspace = null
        }
    }
})
