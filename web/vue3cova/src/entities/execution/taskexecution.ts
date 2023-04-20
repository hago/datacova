import type { CsvFileInfo } from "../datafile/CsvFileInfo"
import type { ExcelFileInfo } from "../datafile/ExcelFileInfo"
import type { ExcelXFileInfo } from "../datafile/ExcelXFileInfo"
import type { ParquetFileInfo } from "../datafile/ParquetFileInfo"
import type { Task } from "../task/task"

export interface TaskExecution {
    id: number
    taskId: number
    task: Task
    addBy: number
    addTime: number
    startTime: number | null
    endTime: number | null
    status: 0 | 1 | 2 | -1
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
