import type { TaskAction } from "./task";

export interface DistributeConfiguraton  {
    type: string
    copyOriginal: boolean
    overwriteExisted: boolean
    targetFileName: string | null
}

export const emptyDistConfiguration = (): DistributeConfiguraton => {
    return {
        type: "",
        copyOriginal: true,
        overwriteExisted: true,
        targetFileName: null
    }
}

export interface TaskActionDistribute extends TaskAction {
    configuration: DistributeConfiguraton
}
