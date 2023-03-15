import type { TaskActionIdle } from "./taskidle"
import type { TaskActionIngest } from "./taskingest"
import type { TaskActionVerify } from "./taskverify"

export interface TaskExtra {
    tag: string
    locale: string
    mailRecipients: string[]
    mailCCRecipients: string[]
    mailBCCRecipients: string[]
}

export const newTaskExtra = (): TaskExtra => {
    return {
        tag: "",
        locale: "en_US",
        mailRecipients: [],
        mailCCRecipients: [],
        mailBCCRecipients: []
    }
}

export interface TaskAction {
    type: -1 | 1 | 2 | 3
    name: string
    description: string
    enabled: boolean
    expand?: boolean
}


export interface Task {
    id: number
    name: string
    description: string
    workspaceId: number
    extra: TaskExtra
    actions: (TaskAction | TaskActionIngest | TaskActionIdle | TaskActionVerify)[],
    addTime: number,
    addBy: number,
    modifyTime: number,
    modifyBy: number
}

export const newEmptyTask = (wkid: number): Task => {
    return {
        id: -1,
        name: "Input Task Name",
        description: "",
        workspaceId: wkid,
        extra: newTaskExtra(),
        actions: [],
        addTime: new Date().getTime(),
        addBy: -1,
        modifyBy: -1,
        modifyTime: 0
    }
}
