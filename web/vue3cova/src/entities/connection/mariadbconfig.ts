import type { BaseDbConfig } from "./dbconfigbase";

export interface MariaDBConfig extends BaseDbConfig {
    host: string
    port: number,
    storeEngine: "innodb" | "MyISAM"
}

export const fromBaseConfig = (baseConfig: BaseDbConfig): MariaDBConfig => {
    let ret = baseConfig as MariaDBConfig
    if (ret.host === undefined) {
        ret.host = ""
    }
    if (ret.port === undefined) {
        ret.port = 5432
    }
    if (ret.storeEngine === undefined) {
        ret.storeEngine = "innodb"
    }
    return ret
}
