export interface ExcelSheetInfo {
    columns: string[]
    rowCount: number
    name: string
}

export interface ExcelInfo {
    sheets: ExcelSheetInfo[]
}
