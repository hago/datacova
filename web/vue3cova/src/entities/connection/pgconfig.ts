import type { BaseDbConfig } from "./dbconfigbase";

export interface PostgreSqlConfig extends BaseDbConfig {
    host: string
    port: number
}

export const fromBaseConfig = (baseConfig: BaseDbConfig): PostgreSqlConfig => {
    let ret = baseConfig as PostgreSqlConfig
    if (ret.host === undefined) {
        ret.host = ""
    }
    if (ret.port === undefined) {
        ret.port = 5432
    }
    return ret
}
