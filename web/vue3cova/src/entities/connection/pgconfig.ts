import type { BaseDbConfig } from "./dbconfigbase";

export interface PostgreSqlConfig extends BaseDbConfig {
    host: string
    port: number
}
