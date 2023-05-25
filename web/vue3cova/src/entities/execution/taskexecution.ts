import type { CsvFileInfo } from "../datafile/CsvFileInfo"
import type { ExcelFileInfo } from "../datafile/ExcelFileInfo"
import type { ExcelXFileInfo } from "../datafile/ExcelXFileInfo"
import type { ParquetFileInfo } from "../datafile/ParquetFileInfo"
import type { Task } from "../task/task"

export type ExecutionStatus = 0 | "0" | 1 | "1" | 2 | "2" | -1 | "-1"

export const isQueueing = (status: ExecutionStatus): boolean => {
    return status === 0 || status === '0'
}

export const isInProcess = (status: ExecutionStatus): boolean => {
    return status === 1 || status === '1'
}

export const isSucceeded = (status: ExecutionStatus): boolean => {
    return status === 2 || status === '2'
}

export const isFailed = (status: ExecutionStatus): boolean => {
    return status === -1 || status === '-1'
}

export const formatStatus = (x: ExecutionStatus): string => {
    switch (x) {
        case 0:
        case "0":
            return "Queueing"
        case 1:
        case "1":
            return "In process"
        case 2:
        case "2":
            return "Succeeded"
        case -1:
        case "-1":
            return "Failed"
        default:
            return `Unknown ${x}`
    }
}

export interface TaskExecution {
    id: number
    taskId: number
    task: Task
    addBy: number
    addTime: number
    startTime: number | null
    endTime: number | null
    status: ExecutionStatus
    fileInfo: ExecutionFileInfo
    detail: ExecutionDetail | null
}

export interface ExecutionFileInfo {
    originalName: string
    size: number
    fileInfo: CsvFileInfo | ExcelFileInfo | ExcelXFileInfo | ParquetFileInfo
}

export interface ExecutionDetail {
    startTime: number
    endTime: number
    lineCount: number
}
