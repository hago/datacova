import type { TaskAction } from "./task";

export interface DistributeConfiguraton  {
    type: string
    copyOriginal: boolean
    overwriteExisted: boolean
    targetFileName: string
}

export interface TaskActionDistribute extends TaskAction {
    configuration: DistributeConfiguraton
}
