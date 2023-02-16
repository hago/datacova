import type { TaskAction } from "./task";

export interface IdleConfiguration {
    stepCount: number
    minMilliSeconds: number
    maxMilliSeconds: number
}

export interface TaskActionIdle extends TaskAction {
    configuration: IdleConfiguration
    executeResult: boolean
    failReason: string
}
