import type { BaseDbConfig } from "./dbconfigbase";

export interface MsSqlConfig extends BaseDbConfig {
    host: string
    port: number
    trustServerCertificate: boolean
}

export const fromBaseConfig = (baseConfig: BaseDbConfig): MsSqlConfig => {
    let ret = baseConfig as MsSqlConfig
    if (ret.host === undefined) {
        ret.host = ""
    }
    if (ret.port === undefined) {
        ret.port = 5432
    }
    if (ret.trustServerCertificate === undefined) {
        ret.trustServerCertificate = false
    }
    return ret
}
