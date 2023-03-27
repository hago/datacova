import type { BaseFileInfo } from "./BaseFileInfo";
import { type ExcelFileInfo, fromBaseFileInfo as f } from "./ExcelFileInfo";

export interface ExcelXFileInfo extends ExcelFileInfo {
    sheetIndex: number | null
    sheetName: string | null
}

export const fromBaseFileInfo = (info: BaseFileInfo): ExcelXFileInfo => {
    let ret = f(info) as ExcelXFileInfo
    ret.type = 3
    return ret
}

export const FILE_TYPE_EXCEL_OOXML = 3
