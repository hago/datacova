import type { BaseFileInfo } from "./BaseFileInfo";

export interface ParquetFileInfo extends BaseFileInfo {
    //
}

export const emptyParquetFileInfo = (): ParquetFileInfo => ({
    type: FILE_TYPE_PARQUET,
    filename: ""
})

export const FILE_TYPE_PARQUET = 4
