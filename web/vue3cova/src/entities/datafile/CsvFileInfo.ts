import type { BaseFileInfo } from "./BaseFileInfo";

export interface CsvFileInfo extends BaseFileInfo {
    quote?: string,
    delimiter: string,
    encoding?: string
}

export const fromBaseFileInfo = (info: BaseFileInfo): CsvFileInfo => {
    let ret = info as CsvFileInfo
    ret.type = 1
    if (ret.delimiter === undefined) {
        ret.delimiter = ','
    }
    return ret
}
