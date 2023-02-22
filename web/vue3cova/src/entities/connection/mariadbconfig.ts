import type { BaseDbConfig } from "./dbconfigbase";

export interface MariaDBConfig extends BaseDbConfig {
    host: string
    port: number,
    storeEngine: string
}
