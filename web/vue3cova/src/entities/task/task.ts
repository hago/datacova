import type { TaskActionIdle } from "./taskidle"
import type { TaskActionIngest } from "./taskingest"

export interface TaskExtra {
    tag: string
    locale: string
    mailRecipients: string[]
    mailCCRecipients: string[]
    mailBCCRecipients: string[]
}

export interface TaskAction {
    type: number
    name: string
    description: string
    enabled: boolean
}


export interface Task {
    id: number
    name: string
    description: string
    workspaceId: number
    extra: TaskExtra
    actions: (TaskActionIngest | TaskActionIdle)[]
}
