import type { TaskAction } from "./task"

export interface IngestOptions {
    targetTable: string
    targetSchema: string
    addBatch: boolean
    clearTable: boolean
    createTableIfNeeded: boolean
    batchColumnName: string
}

export interface TaskActionIngest extends TaskAction {
    connectionId: number
    ingestOptions: IngestOptions
}

export const newTaskActionIngest = (action: TaskAction): TaskActionIngest => {
    let ret = action as TaskActionIngest
    if (ret.connectionId === undefined) {
        ret.connectionId = -1
    }
    if (ret.ingestOptions === undefined) {
        ret.ingestOptions = {
            targetSchema: "",
            targetTable: "",
            addBatch: false,
            clearTable: false,
            createTableIfNeeded: true,
            batchColumnName: "batchId"
        }
    }
    return ret
}