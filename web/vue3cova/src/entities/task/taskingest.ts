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
    connectionId?: number
    ingestOptions?: IngestOptions
}
