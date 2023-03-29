import type { BaseFileInfo } from "./BaseFileInfo";

export interface ExcelFileInfo extends BaseFileInfo {
    sheetIndex: number | null
    sheetName: string | null
}

export const fromBaseFileInfo = (info: BaseFileInfo): ExcelFileInfo => {
    let ret = info as ExcelFileInfo
    ret.type = 2
    if (ret.sheetIndex === undefined) {
        ret.sheetIndex = null
    }
    if (ret.sheetName === undefined) {
        ret.sheetName = null
    }
    return ret
}

export const emptyExcelFileInfo = (): ExcelFileInfo => ({
    filename: '',
    type: 2,
    sheetIndex: 0,
    sheetName: null
})

export const FILE_TYPE_EXCEL = 2
